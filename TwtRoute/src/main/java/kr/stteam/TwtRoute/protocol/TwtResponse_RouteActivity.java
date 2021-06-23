package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TwtResponse_RouteActivity {
    String task_id;
    String task_type;
    double[] loc_coord;
    String  loc_name;
    double      duration;
    double      distance;
    int         req_task_index;
    int         task_order;
    double        arrival_time;
    double        end_time;



    @JsonProperty("time_window")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private double[] time_window;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    TripGeometry geometry ;

    DebugRouteActivity debug;
}
