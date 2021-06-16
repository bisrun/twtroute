package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsrmRouteResponseParam_Base {
    @JsonProperty("code")
    String code;
    @JsonProperty("waypoints")
    ArrayList<OsrmRouteResponseParam_Waypoint> waypoints;
    @JsonProperty("routes")
    ArrayList<OsrmRouteResponseParam_Route> routes;
}
