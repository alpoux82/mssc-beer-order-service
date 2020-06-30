package guru.springframework.msscbeerorderservice.services.testComponents;

import guru.sfg.brewery.model.events.ValidateOrderRequest;
import guru.sfg.brewery.model.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import static guru.springframework.msscbeerorderservice.config.JmsConfig.VALIDATE_ORDER_QUEUE;
import static guru.springframework.msscbeerorderservice.config.JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE;

@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = VALIDATE_ORDER_QUEUE)
    private void listen(Message message) {
        ValidateOrderRequest request = (ValidateOrderRequest) message.getPayload();

        boolean isValid = false;
        boolean sendResponse = true;

        if (checkParameter(request, "fail-validation"))
            isValid = false;
        else if (checkParameter(request, "pass-validation"))
            isValid = true;
        else if (checkParameter(request, "dont-validate"))
            sendResponse = false;

        if (sendResponse)
            jmsTemplate.convertAndSend(VALIDATE_ORDER_RESPONSE_QUEUE,
                    ValidateOrderResult.builder().orderId(request.getBeerOrderDto().getId()).isValid(isValid).build());
    }

    private boolean checkParameter(ValidateOrderRequest request, String s) {
        return request.getBeerOrderDto().getCustomerRef() != null &&
                request.getBeerOrderDto().getCustomerRef().equalsIgnoreCase(s);
    }
}
