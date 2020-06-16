package guru.springframework.msscbeerorderservice.domain;

public enum BeerOrderEvent {
    VALIDATE_ORDER, VALIDATION_PASSED, VALIDATION_FAILED,
    ALLOCATION_SUCCESS, ALLOCATION_NO_INVENTORY, ALLOCATION_FAILED,
    BEER_ORDER_PICKED_UP
}
