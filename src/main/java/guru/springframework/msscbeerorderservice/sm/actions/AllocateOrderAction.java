package guru.springframework.msscbeerorderservice.sm.actions;

import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.springframework.msscbeerorderservice.domain.BeerOrder;
import guru.springframework.msscbeerorderservice.domain.BeerOrderEvent;
import guru.springframework.msscbeerorderservice.domain.BeerOrderStatus;
import guru.springframework.msscbeerorderservice.repositories.BeerOrderRepository;
import guru.springframework.msscbeerorderservice.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import static guru.springframework.msscbeerorderservice.config.JmsConfig.ALLOCATE_ORDER_QUEUE;
import static guru.springframework.msscbeerorderservice.services.BeerOrderManagerImpl.ORDER_ID_HEADER;

@RequiredArgsConstructor
@Component
public class AllocateOrderAction implements Action<BeerOrderStatus, BeerOrderEvent> {

    private final BeerOrderRepository beerOrderRepository;
    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> stateContext) {
        String beerOrderId = (String) stateContext.getMessageHeader(ORDER_ID_HEADER);
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(UUID.fromString(beerOrderId));

        beerOrderOptional.ifPresent(beerOrder -> jmsTemplate.convertAndSend(ALLOCATE_ORDER_QUEUE,
                AllocateOrderRequest.builder()
                        .beerOrderDto(beerOrderMapper.beerOrderToBeerOrderDto(beerOrder))
                        .build())
        );
    }
}
