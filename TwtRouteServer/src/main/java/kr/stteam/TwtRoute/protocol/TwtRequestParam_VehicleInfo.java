package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TwtRequestParam_VehicleInfo {
    @JsonProperty("driver")
    private String driver;

    @JsonProperty("vehicleid")
    private String vehicle_id;

    @JsonProperty("starttime")
    private String starttime;

    @JsonProperty("endtime")
    private String endtime;
}
