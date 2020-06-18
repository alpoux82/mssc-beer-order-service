package guru.springframework.msscbeerorderservice.services;

import guru.springframework.msscbeerorderservice.domain.BeerOrder;

public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder beerOrder);
}
