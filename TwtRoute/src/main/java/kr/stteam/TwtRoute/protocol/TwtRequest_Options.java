package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;



@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwtRequest_Options {
    @JsonProperty("service_time")
    private double service_time;

    @JsonProperty("req_route_geom")
    private boolean req_route_geom;

    @JsonProperty("req_debuginfo")
    private boolean req_debuginfo;

    @JsonProperty("keep_entry_angle")
    private boolean keep_entry_angle;
}
