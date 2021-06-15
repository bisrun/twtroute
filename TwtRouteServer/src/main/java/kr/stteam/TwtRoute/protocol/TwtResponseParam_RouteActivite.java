package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TwtResponseParam_RouteActivite {
    String task_id;

    double[] point ;
    String loc_name;
    double tms_duration;
    double      distance;
    double      weight;
    int         req_task_index;
    int         task_order;
    String      tm_arrival;
    String      tm_end;
    double tms_arrival;
    double tms_end;
    @JsonProperty("timewindow")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> timewindow;
    @JsonIgnore
    double[] tms_timewindow;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    TripGeometry geometry ;
}
