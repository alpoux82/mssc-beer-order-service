package guru.springframework.msscbeerorderservice.services;

import guru.sfg.brewery.model.BeerOrderDto;
import guru.springframework.msscbeerorderservice.domain.BeerOrder;
import guru.springframework.msscbeerorderservice.domain.BeerOrderEvent;
import guru.springframework.msscbeerorderservice.domain.BeerOrderStatus;
import guru.springframework.msscbeerorderservice.repositories.BeerOrderRepository;
import guru.springframework.msscbeerorderservice.sm.BeerOrderStateChangeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static guru.springframework.msscbeerorderservice.domain.BeerOrderEvent.*;
import static guru.springframework.msscbeerorderservice.domain.BeerOrderStatus.*;

@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";

    private final StateMachineFactory<BeerOrderStatus, BeerOrderEvent> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        // defensive programming, set values to initial default
        beerOrder.setId(null);
        beerOrder.setBeerOrderStatus(NEW);

        BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);
        sendBeerOrderEvent(savedBeerOrder, VALIDATE_ORDER);
        return savedBeerOrder;
    }

    @Transactional
    @Override
    public void processValidationResult(UUID beerOrderId, Boolean isValid) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderId);
        beerOrderOptional.ifPresent(beerOrder -> {
            if (isValid) {
                sendBeerOrderEvent(beerOrder, VALIDATION_PASSED);
                //volver a recuperarla de bbdd porque el interceptor la coge y la guarda
                // Hibernate detecta una nueva versión del objeto
                awaitForStatus(beerOrderId, VALIDATED);
                BeerOrder validatedOrder = beerOrderRepository.findById(beerOrderId).get();
                sendBeerOrderEvent(validatedOrder, ALLOCATE_ORDER);
            }
            else
                sendBeerOrderEvent(beerOrder, VALIDATION_FAILED);
        });
    }

    @Override
    public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        beerOrderOptional.ifPresent(beerOrder -> {
            sendBeerOrderEvent(beerOrder, ALLOCATION_SUCCESS);
            awaitForStatus(beerOrder.getId(), ALLOCATED);
            updateAllocatedQty(beerOrderDto);
        });
    }

    @Override
    public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        beerOrderOptional.ifPresent(beerOrder -> {
            sendBeerOrderEvent(beerOrder, ALLOCATION_NO_INVENTORY);
            awaitForStatus(beerOrder.getId(), PENDING_INVENTORY);
            updateAllocatedQty(beerOrderDto);
        });
    }

    @Override
    public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        beerOrderOptional.ifPresent(beerOrder -> sendBeerOrderEvent(beerOrder, ALLOCATION_FAILED));
    }

    @Override
    public void beerOrderPickedUp(UUID id) {
        beerOrderRepository.findById(id).ifPresent(beerOrder -> sendBeerOrderEvent(beerOrder, BEER_ORDER_PICKED_UP));
    }

    @Override
    public void cancelOrder(UUID id) {
        beerOrderRepository.findById(id).ifPresent(beerOrder -> sendBeerOrderEvent(beerOrder, CANCEL_ORDER));
    }

    private void updateAllocatedQty(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> allocatedOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        allocatedOrderOptional.ifPresent(allocatedOrder -> {
            allocatedOrder.getBeerOrderLines().forEach(beerOrderLine ->
                    beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
                        if (beerOrderLine.getId().equals(beerOrderLineDto.getId()))
                            beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
                    })
            );
            beerOrderRepository.saveAndFlush(allocatedOrder);
        });
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEvent event) {
        StateMachine<BeerOrderStatus, BeerOrderEvent> sm = build(beerOrder);
        Message<BeerOrderEvent> msg = MessageBuilder
                .withPayload(event)
                .setHeader(ORDER_ID_HEADER, beerOrder.getId().toString())
                .build();
        sm.sendEvent(msg);
    }

    private void awaitForStatus(UUID beerOrderId, BeerOrderStatus beerOrderStatus) {
        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger loopCount = new AtomicInteger(0);

        while (!found.get()) {
            if (loopCount.incrementAndGet() > 10)
                found.set(true);
            beerOrderRepository.findById(beerOrderId).ifPresent( beerOrder -> {
                if (beerOrder.getBeerOrderStatus().equals(beerOrderStatus))
                    found.set(true);
            });
            if (!found.get())
                try {
                    Thread.sleep(100);
                } catch (Exception e) {

                }
        }
    }

    private StateMachine<BeerOrderStatus, BeerOrderEvent> build(BeerOrder beerOrder) {
        StateMachine<BeerOrderStatus, BeerOrderEvent> sm = stateMachineFactory.getStateMachine(beerOrder.getId());
        sm.stop();
        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getBeerOrderStatus(), null, null, null));
                });
        sm.start();
        return sm;
    }
}
