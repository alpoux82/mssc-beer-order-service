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
        request.getBeerOrderDto().getBeerOrderLines().forEach(beerOrderLineDto ->
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity()));
        jmsTemplate.convertAndSend(ALLOCATE_ORDER_RESPONSE_QUEUE,
                AllocateOrderResult.builder()
                        .beerOrderDto(request.getBeerOrderDto())
                        .pendingInventory(false)
                        .allocationError(false)
                        .build()
        );
    }
}
