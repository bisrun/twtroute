package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsrmRouteResponseParam_Waypoint {
    @JsonProperty("distance")
    double  distance;

    @JsonProperty("location")
    double[] location ;

    @JsonIgnore
    int     start_coord_idx;

    @JsonIgnore
    int     coord_count;
}
