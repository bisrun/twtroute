package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TwtResponse_SolutionRoute {
    String      vehicle_id;
    double      distance;
    double      duration;

    double      start_time;
    double      end_time;

    DebugRouteActivity debug;
    ArrayList<TwtResponse_RouteActivity> activities ;
}
