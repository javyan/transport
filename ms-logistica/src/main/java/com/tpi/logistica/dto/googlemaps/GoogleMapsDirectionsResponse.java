package com.tpi.logistica.dto.googlemaps;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class GoogleMapsDirectionsResponse {
    
    @JsonProperty("routes")
    private List<Route> routes;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("error_message")
    private String errorMessage;
}
