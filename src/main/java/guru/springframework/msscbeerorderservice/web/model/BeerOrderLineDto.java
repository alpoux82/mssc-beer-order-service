package guru.springframework.msscbeerorderservice.web.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BeerOrderLineDto extends BaseItem {

    @Builder
    public BeerOrderLineDto(UUID id, Integer version, OffsetDateTime createdDate, OffsetDateTime lastModifiedDate,
                            String upc, String beerName, String beerStyle, UUID beerId, Integer orderQuantity,
                            BigDecimal price) {
        super(id, version, createdDate, lastModifiedDate);
        this.upc = upc;
        this.beerName = beerName;
        this.BeerStyle = beerStyle;
        this.beerId = beerId;
        this.orderQuantity = orderQuantity;
        this.price = price;
    }

    private String upc;
    private String beerName;
    private String BeerStyle;
    private UUID beerId;
    private Integer orderQuantity = 0;
    private BigDecimal price;
}
