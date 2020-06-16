package guru.springframework.msscbeerorderservice.domain;

public enum BeerOrderStatus {
    NEW, VALIDATED, VALIDATION_EXCEPTION, ALLOCATED, ALLOCATION_EXCEPTION, PENDING_INVENTORY, PICKED_UP, DELIVERED, DELIVERY_EXCEPTION
}
