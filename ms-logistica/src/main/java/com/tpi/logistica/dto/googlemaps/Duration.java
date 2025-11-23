package com.tpi.logistica.dto.googlemaps;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Duration {
    
    @JsonProperty("text")
    private String text;
    
    @JsonProperty("value")
    private Integer value; // Duración en segundos
}
