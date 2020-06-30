package guru.springframework.msscbeerorderservice.sm.actions;

import guru.springframework.msscbeerorderservice.domain.BeerOrderEvent;
import guru.springframework.msscbeerorderservice.domain.BeerOrderStatus;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Component
public class ValidationFailureAction implements Action<BeerOrderStatus, BeerOrderEvent> {
    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> stateContext) {
        //TODO include logic
    }
}
