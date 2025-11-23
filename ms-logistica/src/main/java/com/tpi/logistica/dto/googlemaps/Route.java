package com.tpi.logistica.dto.googlemaps;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class Route {
    
    @JsonProperty("legs")
    private List<Leg> legs;
    
    @JsonProperty("overview_polyline")
    private OverviewPolyline overviewPolyline;
    
    @JsonProperty("summary")
    private String summary;
    
    @JsonProperty("warnings")
    private List<String> warnings;
}
