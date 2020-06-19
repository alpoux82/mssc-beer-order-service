package guru.springframework.msscbeerorderservice.services.listeners;

import guru.sfg.brewery.model.events.AllocateOrderResult;
import guru.springframework.msscbeerorderservice.services.BeerOrderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import static guru.springframework.msscbeerorderservice.config.JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE;

@RequiredArgsConstructor
@Component
public class AllocationResultListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void listen(AllocateOrderResult allocateOrderResult) {
        if (!allocateOrderResult.getAllocationError() && !allocateOrderResult.getPendingInventory())
            beerOrderManager.beerOrderAllocationPassed(allocateOrderResult.getBeerOrderDto());
        else if (!allocateOrderResult.getAllocationError() && allocateOrderResult.getPendingInventory())
            beerOrderManager.beerOrderAllocationPendingInventory(allocateOrderResult.getBeerOrderDto());
        else if (allocateOrderResult.getAllocationError())
            beerOrderManager.beerOrderAllocationFailed(allocateOrderResult.getBeerOrderDto());
    }
}
