package guru.springframework.msscbeerorderservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import guru.springframework.msscbeerorderservice.domain.BeerOrder;
import guru.springframework.msscbeerorderservice.domain.BeerOrderLine;
import guru.springframework.msscbeerorderservice.domain.Customer;
import guru.springframework.msscbeerorderservice.repositories.BeerOrderRepository;
import guru.springframework.msscbeerorderservice.repositories.CustomerRepository;
import guru.springframework.msscbeerorderservice.services.beer.BeerServiceImpl;
import guru.springframework.msscbeerorderservice.services.beer.model.BeerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static guru.springframework.msscbeerorderservice.domain.BeerOrderStatus.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(WireMockExtension.class)
@SpringBootTest
public class BeerOrderManagerImplIT {

    @Autowired
    BeerOrderManager beerOrderManager;
    @Autowired
    BeerOrderRepository beerOrderRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    WireMockServer wireMockServer;
    Customer testCustomer;
    UUID beerId = UUID.randomUUID();

    @TestConfiguration
    static class RestTemplateBuilderProvider {
        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer() {
            WireMockServer server = with(wireMockConfig().port(8083));
            server.start();
            return server;
        }
    }

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.save(Customer.builder().customerName("Test Customer").build());
    }

    @Test
    void testNewToAllocated() throws JsonProcessingException, InterruptedException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();
        wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef("pass-validation");
        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundBeerOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            assertEquals(ALLOCATED, foundBeerOrder.getBeerOrderStatus());
        });
        await().untilAsserted(() -> {
            BeerOrder foundBeerOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            BeerOrderLine line = foundBeerOrder.getBeerOrderLines().iterator().next();
            assertEquals(line.getOrderQuantity(), line.getQuantityAllocated());
        });

        BeerOrder savedBeerOrder2 = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertNotNull(savedBeerOrder2);
        assertEquals(ALLOCATED, savedBeerOrder2.getBeerOrderStatus());
        savedBeerOrder2.getBeerOrderLines().forEach(line ->
                assertEquals(line.getOrderQuantity(), line.getQuantityAllocated())
        );
    }

    @Test
    void testNewToPickedUp() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();
        wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef("pass-validation");
        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundBeerOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            assertEquals(ALLOCATED, foundBeerOrder.getBeerOrderStatus());
        });

        beerOrderManager.beerOrderPickedUp(savedBeerOrder.getId());

        await().untilAsserted(() -> {
            BeerOrder foundBeerOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            assertEquals(PICKED_UP, foundBeerOrder.getBeerOrderStatus());
        });

        BeerOrder pickedUpOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
        assertEquals(PICKED_UP, pickedUpOrder.getBeerOrderStatus());
    }

    @Test
    void testNewToValidationException() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();
        wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "12345").willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef("fail-validation");
        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundBeerOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            assertEquals(VALIDATION_EXCEPTION, foundBeerOrder.getBeerOrderStatus());
        });
    }

    public BeerOrder createBeerOrder() {
        BeerOrder beerOrder = BeerOrder.builder().customer(testCustomer).build();
        Set<BeerOrderLine> lines = new HashSet<>();
        lines.add(BeerOrderLine.builder().beerId(beerId).upc("12345").orderQuantity(1).beerOrder(beerOrder).build());
        beerOrder.setBeerOrderLines(lines);

        return beerOrder;
    }
}