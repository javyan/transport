package com.tpi.logistica.dto.googlemaps;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OverviewPolyline {
    
    @JsonProperty("points")
    private String points;
}
