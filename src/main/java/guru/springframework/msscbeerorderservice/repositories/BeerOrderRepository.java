package guru.springframework.msscbeerorderservice.repositories;

import guru.springframework.msscbeerorderservice.domain.BeerOrder;
import guru.springframework.msscbeerorderservice.domain.BeerOrderStatus;
import guru.springframework.msscbeerorderservice.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BeerOrderRepository extends JpaRepository<BeerOrder, UUID> {

    Page<BeerOrder> findAllByCustomer(Customer customer, Pageable pageable);

    List<BeerOrder> findAllByBeerOrderStatus(BeerOrderStatus beerOrderStatus);

}
