package guru.springframework.msscbeerorderservice.sm;

import guru.springframework.msscbeerorderservice.domain.BeerOrder;
import guru.springframework.msscbeerorderservice.domain.BeerOrderEvent;
import guru.springframework.msscbeerorderservice.domain.BeerOrderStatus;
import guru.springframework.msscbeerorderservice.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static guru.springframework.msscbeerorderservice.services.BeerOrderManagerImpl.ORDER_ID_HEADER;

@RequiredArgsConstructor
@Component
public class BeerOrderStateChangeInterceptor extends StateMachineInterceptorAdapter<BeerOrderStatus, BeerOrderEvent> {

    private final BeerOrderRepository beerOrderRepository;

    @Transactional
    @Override
    public void preStateChange(State<BeerOrderStatus, BeerOrderEvent> state, Message<BeerOrderEvent> message,
                               Transition<BeerOrderStatus, BeerOrderEvent> transition, StateMachine<BeerOrderStatus, BeerOrderEvent> stateMachine) {
        Optional.ofNullable(message)
                .flatMap(msg -> Optional.ofNullable(msg.getHeaders().getOrDefault(ORDER_ID_HEADER, "")))
                .ifPresent(orderId -> {
                    BeerOrder beerOrder = beerOrderRepository.getOne(UUID.fromString(orderId.toString()));
                    beerOrder.setBeerOrderStatus(state.getId());
                    beerOrderRepository.saveAndFlush(beerOrder);
                });
    }
}
