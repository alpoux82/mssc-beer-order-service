package guru.springframework.msscbeerorderservice.sm.actions;

import guru.sfg.brewery.model.events.AllocationFailureEvent;
import guru.springframework.msscbeerorderservice.domain.BeerOrderEvent;
import guru.springframework.msscbeerorderservice.domain.BeerOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static guru.springframework.msscbeerorderservice.config.JmsConfig.ALLOCATION_FAILURE_QUEUE;
import static guru.springframework.msscbeerorderservice.services.BeerOrderManagerImpl.ORDER_ID_HEADER;

@RequiredArgsConstructor
@Component
public class AllocationFailureAction implements Action<BeerOrderStatus, BeerOrderEvent> {

    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> stateContext) {
        //String beerOrderId = (String) stateContext.getMessageHeader(ORDER_ID_HEADER);
        String beerOrderId = (String) stateContext.getMessage().getHeaders().get(ORDER_ID_HEADER);

        jmsTemplate.convertAndSend(ALLOCATION_FAILURE_QUEUE,
                AllocationFailureEvent.builder().orderId(UUID.fromString(beerOrderId)).build());
    }
}
