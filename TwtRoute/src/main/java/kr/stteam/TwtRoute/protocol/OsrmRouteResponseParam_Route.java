package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsrmRouteResponseParam_Route {
    @JsonProperty("legs")
    ArrayList<OsrmRouteResponseParam_RouteLeg> legs;

    @JsonProperty("geometry")
    TripGeometry geometry;

    @JsonProperty("weight")
    double weight;
    @JsonProperty("distance")
    double distance;
    @JsonProperty("duration")
    double duration;
}
