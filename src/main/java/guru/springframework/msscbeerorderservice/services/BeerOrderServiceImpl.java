package guru.springframework.msscbeerorderservice.services;

import guru.springframework.msscbeerorderservice.domain.BeerOrder;
import guru.springframework.msscbeerorderservice.domain.Customer;
import guru.springframework.msscbeerorderservice.domain.BeerOrderStatus;
import guru.springframework.msscbeerorderservice.repositories.BeerOrderRepository;
import guru.springframework.msscbeerorderservice.repositories.CustomerRepository;
import guru.springframework.msscbeerorderservice.web.mappers.BeerOrderMapper;
import guru.springframework.msscbeerorderservice.web.model.BeerOrderDto;
import guru.springframework.msscbeerorderservice.web.model.BeerOrderPagedList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BeerOrderServiceImpl implements BeerOrderService {

    private final BeerOrderRepository beerOrderRepository;
    private final CustomerRepository customerRepository;
    private final BeerOrderMapper beerOrderMapper;

    public BeerOrderServiceImpl(BeerOrderRepository beerOrderRepository, CustomerRepository customerRepository, BeerOrderMapper beerOrderMapper) {
        this.beerOrderRepository = beerOrderRepository;
        this.customerRepository = customerRepository;
        this.beerOrderMapper = beerOrderMapper;
    }

    @Override
    public BeerOrderPagedList listOrders(UUID customerId, Pageable pageable) {

        Page<BeerOrder> beerOrderPage = beerOrderRepository.findAllByCustomer(
                customerRepository.findById(customerId).orElse(null), pageable);
        return new BeerOrderPagedList(beerOrderPage.stream()
                .map(beerOrderMapper::beerOrderToBeerOrderDto)
                .collect(Collectors.toList()), pageable, beerOrderPage.getTotalElements());
    }

    @Override
    public BeerOrderDto placeOrder(UUID customerId, BeerOrderDto beerOrderDto) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(NoSuchElementException::new);
        BeerOrder beerOrder = beerOrderMapper.beerOrderDtoToBeerOrder(beerOrderDto);
        beerOrder.setId(null);
        beerOrder.setCustomer(customer);
        beerOrder.setBeerOrderStatus(BeerOrderStatus.NEW);
        beerOrder.getBeerOrderLines().forEach(line -> line.setBeerOrder(beerOrder));

        return beerOrderMapper.beerOrderToBeerOrderDto(beerOrderRepository.saveAndFlush(beerOrder));
    }

    @Override
    public BeerOrderDto getOrderById(UUID customerId, UUID orderId) {
        return beerOrderMapper.beerOrderToBeerOrderDto(getCustomerOrder(customerId, orderId)
                .orElseThrow(NoSuchElementException::new));
    }

    @Override
    public void pickupOrder(UUID customerId, UUID orderId) {
        BeerOrder beerOrder = getCustomerOrder(customerId, orderId).orElseThrow(NoSuchElementException::new);
        beerOrder.setBeerOrderStatus(BeerOrderStatus.PICKED_UP);
        beerOrderRepository.save(beerOrder);
    }

    private Optional<BeerOrder> getCustomerOrder(UUID customerId, UUID orderId) {
        customerRepository.findById(customerId).orElseThrow(NoSuchElementException::new);
        return beerOrderRepository.findById(orderId).filter(order -> order.getCustomer().getId().equals(customerId));
    }
}
