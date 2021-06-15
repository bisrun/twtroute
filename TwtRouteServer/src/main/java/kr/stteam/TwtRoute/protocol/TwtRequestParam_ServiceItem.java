package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


import java.util.List;


@Data
public class TwtRequestParam_ServiceItem {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("pos")
    private List<Double> pos ;

    @JsonProperty("svctime")
    private int svctime;

    @JsonProperty("timewindow")
    private List<String> timewindow;
}
