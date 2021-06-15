package kr.stteam.TwtRoute.protocol;

import lombok.Data;

import java.util.ArrayList;
@Data
public class TwtResponseParam_SolutionRoute {
    String vehicle_id;
    double     costs;
    double     distance;
    double tms_time;
    double tms_transport;
    double tms_completion;
    ArrayList<TwtResponseParam_RouteActivite> activities ;


}
