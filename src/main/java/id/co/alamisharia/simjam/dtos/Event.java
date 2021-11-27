package id.co.alamisharia.simjam.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class Event implements Serializable {

    @JsonProperty("upc")
    private String upc;
    @JsonProperty("store_number")
    private String storeNumber;
    @JsonProperty("status")
    private String status;
}
