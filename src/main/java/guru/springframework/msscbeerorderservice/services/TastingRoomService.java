package guru.springframework.msscbeerorderservice.services;

import guru.springframework.msscbeerorderservice.bootstrap.BeerOrderBootstrap;
import guru.springframework.msscbeerorderservice.domain.Customer;
import guru.springframework.msscbeerorderservice.repositories.BeerOrderRepository;
import guru.springframework.msscbeerorderservice.repositories.CustomerRepository;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.BeerOrderLineDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class TastingRoomService {

    private final CustomerRepository customerRepository;
    private final BeerOrderService beerOrderService;
    private final BeerOrderRepository beerOrderRepository;
    private final List<String> beerUpcList = new ArrayList<>(3);

    public TastingRoomService(CustomerRepository customerRepository, BeerOrderService beerOrderService, BeerOrderRepository beerOrderRepository) {
        this.customerRepository = customerRepository;
        this.beerOrderService = beerOrderService;
        this.beerOrderRepository = beerOrderRepository;

        beerUpcList.add(BeerOrderBootstrap.BEER_1_UPC);
        beerUpcList.add(BeerOrderBootstrap.BEER_2_UPC);
        beerUpcList.add(BeerOrderBootstrap.BEER_3_UPC);
    }

    @Transactional
    @Scheduled(fixedRate = 2000)
    public void placeTastingRoomOrder() {
        List<Customer> customerList = customerRepository.findAllByCustomerNameLike(BeerOrderBootstrap.TASTING_ROOM);
        if (customerList.size() == 1) {
            doPlaceORder(customerList.get(0));
        }
        else {
            log.error("Too many or to few tasting room customers found");
        }
    }

    private void doPlaceORder(Customer customer) {
        String beerToOrder = getRandomBeerUpc();
        BeerOrderLineDto beerOrderLineDto = BeerOrderLineDto.builder()
                .upc(beerToOrder)
                .orderQuantity(new Random().nextInt(6))
                .build();
        List<BeerOrderLineDto> beerOrderLineDtoList = new ArrayList<>();
        beerOrderLineDtoList.add(beerOrderLineDto);
        BeerOrderDto beerOrderDto = BeerOrderDto.builder()
                .customerId(customer.getId())
                .customerRef(UUID.randomUUID().toString())
                .beerOrderLines(beerOrderLineDtoList)
                .build();
        beerOrderService.placeOrder(customer.getId(), beerOrderDto);
    }

    private String getRandomBeerUpc() {
        return beerUpcList.get(new Random().nextInt(beerUpcList.size()));
    }
}
