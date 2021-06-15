package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsrmRouteResponseParam_RouteLeg {
    @JsonProperty("weight")
    double weight;
    @JsonProperty("distance")
    double distance;
    @JsonProperty("duration")
    double duration;
}
