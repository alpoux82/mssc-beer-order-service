package guru.springframework.msscbeerorderservice.services.testComponents;

import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import static guru.springframework.msscbeerorderservice.config.JmsConfig.ALLOCATE_ORDER_QUEUE;
import static guru.springframework.msscbeerorderservice.config.JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE;

@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = ALLOCATE_ORDER_QUEUE)
    public void listen(Message message) {
        AllocateOrderRequest request = (AllocateOrderRequest) message.getPayload();
        boolean pendingInventory = false;
        boolean allocationError = false;
        boolean sendResponse = true;

        if (checkParameter(request, "fail-allocation"))
            allocationError =  true;
        if (checkParameter(request, "partial-allocation"))
            pendingInventory =  true;
        if (checkParameter(request, "dont-allocate"))
            sendResponse = false;


        request.getBeerOrderDto().getBeerOrderLines().forEach(beerOrderLineDto ->
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity()));

        if (sendResponse)
            jmsTemplate.convertAndSend(ALLOCATE_ORDER_RESPONSE_QUEUE,
                    AllocateOrderResult.builder()
                            .beerOrderDto(request.getBeerOrderDto())
                            .pendingInventory(pendingInventory)
                            .allocationError(allocationError)
                            .build()
        );
    }

    private boolean checkParameter(AllocateOrderRequest request, String s) {
        return request.getBeerOrderDto().getCustomerRef() != null &&
                request.getBeerOrderDto().getCustomerRef().equalsIgnoreCase(s);
    }
}
