package guru.springframework.msscbeerorderservice.sm;

import guru.springframework.msscbeerorderservice.domain.BeerOrderEvent;
import guru.springframework.msscbeerorderservice.domain.BeerOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

import static guru.springframework.msscbeerorderservice.domain.BeerOrderEvent.*;
import static guru.springframework.msscbeerorderservice.domain.BeerOrderStatus.*;

@RequiredArgsConstructor
@EnableStateMachineFactory
@Configuration
public class BeerOrderStateMachineConfig extends StateMachineConfigurerAdapter<BeerOrderStatus, BeerOrderEvent> {

    private final Action<BeerOrderStatus, BeerOrderEvent> validateOrderAction;
    private final Action<BeerOrderStatus, BeerOrderEvent> allocateOrderAction;

    @Override
    public void configure(StateMachineStateConfigurer<BeerOrderStatus, BeerOrderEvent> states) throws Exception {
        states.withStates()
                .initial(NEW)
                .states(EnumSet.allOf(BeerOrderStatus.class))
                .end(BeerOrderStatus.PICKED_UP)
                .end(BeerOrderStatus.DELIVERED)
                .end(BeerOrderStatus.DELIVERY_EXCEPTION)
                .end(BeerOrderStatus.VALIDATION_EXCEPTION)
                .end(BeerOrderStatus.ALLOCATION_EXCEPTION);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<BeerOrderStatus, BeerOrderEvent> transitions) throws Exception {
        transitions.withExternal()
                    .source(NEW).target(VALIDATION_PENDING).event(VALIDATE_ORDER).action(validateOrderAction)
                .and().withExternal()
                    .source(VALIDATION_PENDING).target(VALIDATED).event(VALIDATION_PASSED)
                .and().withExternal()
                    .source(VALIDATION_PENDING).target(VALIDATION_EXCEPTION).event(VALIDATION_FAILED)
                .and().withExternal()
                    .source(VALIDATED).target(ALLOCATION_PENDING).event(ALLOCATE_ORDER).action(allocateOrderAction)
                .and().withExternal()
                    .source(ALLOCATION_PENDING).target(ALLOCATED).event(ALLOCATION_SUCCESS)
                .and().withExternal()
                    .source(ALLOCATION_PENDING).target(ALLOCATION_EXCEPTION).event(ALLOCATION_FAILED)
                .and().withExternal()
                    .source(ALLOCATION_PENDING).target(PENDING_INVENTORY).event(ALLOCATION_NO_INVENTORY)
                .and().withExternal()
                    .source(ALLOCATED).target(PICKED_UP).event(BEER_ORDER_PICKED_UP);
    }
}
