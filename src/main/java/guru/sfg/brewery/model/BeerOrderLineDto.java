package guru.sfg.brewery.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeerOrderLineDto {

    @JsonProperty("id")
    private UUID id = null;
    @JsonProperty("version")
    private Integer version = null;
    @JsonProperty("createdDate")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ", shape = JsonFormat.Shape.STRING)
    private OffsetDateTime createdDate = null;
    @JsonProperty("lastModifiedDate")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ", shape = JsonFormat.Shape.STRING)
    private OffsetDateTime lastModifiedDate = null;

    private String upc;
    private String beerName;
    private String BeerStyle;
    private UUID beerId;
    private Integer orderQuantity = 0;
    private BigDecimal price;
}
