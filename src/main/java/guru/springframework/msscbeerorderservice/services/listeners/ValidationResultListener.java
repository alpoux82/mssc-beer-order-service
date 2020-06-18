package guru.springframework.msscbeerorderservice.services.listeners;

import guru.sfg.brewery.model.events.ValidateOrderResult;
import guru.springframework.msscbeerorderservice.services.BeerOrderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static guru.springframework.msscbeerorderservice.config.JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE;

@RequiredArgsConstructor
@Component
public class ValidationResultListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = VALIDATE_ORDER_RESPONSE_QUEUE)
    public void listen(ValidateOrderResult validateOrderResult) {
        final UUID beerOrderId = validateOrderResult.getOrderId();
        beerOrderManager.processValidationResult(beerOrderId, validateOrderResult.getIsValid());
    }
}
