package com.tpi.logistica.dto.googlemaps;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Distance {
    
    @JsonProperty("text")
    private String text;
    
    @JsonProperty("value")
    private Integer value; // Distancia en metros
}
