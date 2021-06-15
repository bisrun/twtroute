package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TwtRequestParam_BaseData {
    @JsonProperty("vehicle")
    private TwtRequestParam_VehicleInfo vehicle;

    @JsonProperty("services")
    private List<TwtRequestParam_ServiceItem> services;
}
