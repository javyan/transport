package com.tpi.logistica.dto.googlemaps;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Leg {
    
    @JsonProperty("distance")
    private Distance distance;
    
    @JsonProperty("duration")
    private Duration duration;
    
    @JsonProperty("start_address")
    private String startAddress;
    
    @JsonProperty("end_address")
    private String endAddress;
    
    @JsonProperty("start_location")
    private Location startLocation;
    
    @JsonProperty("end_location")
    private Location endLocation;
}
