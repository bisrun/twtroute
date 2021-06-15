package kr.stteam.TwtRoute.protocol;

import lombok.Data;

import java.util.ArrayList;
@Data
public class TwtResponseParam_Solution {
    double     costs;
    double     distance;
    double tms_time;
    double tms_transport;
    double tms_completion;
    double     unassigned_cnt;
    ArrayList<TwtResponseParam_SolutionRoute> routes ;
    ArrayList<TwtResponseParam_SolutionUnassigned> unassigned ;

}
