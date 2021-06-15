package kr.stteam.TwtRoute.controller;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import kr.stteam.TwtRoute.protocol.*;
import kr.stteam.TwtRoute.protocol.OsrmTripMatrixResponseParam;
import kr.stteam.TwtRoute.service.TwtTaskItem;

import java.util.ArrayList;

public class TwtResult {
    public TwtRequestParam_BaseData requestParam = null;
    public ArrayList<TwtTaskItem> tasklist = null; // ordering 되어있음
    public OsrmTripMatrixResponseParam tripMatrix =null;
    public VehicleRoutingProblem problem= null;
    public VehicleRoutingProblemSolution solution= null;
    public ArrayList<TwtResponseParam_SolutionUnassigned> unassigned_task = null;
    public OsrmRouteResponseParam_Base osrmRouteResponse = null;
    public TwtResponseParam_Base twtResponse;
    public String jsonResult;

    private TwtResult(TwtRequestParam_BaseData requestParam) {
        this.requestParam = requestParam;
    }

    public static TwtResult create(TwtRequestParam_BaseData requestParam) {
        return new TwtResult(requestParam);
    }
    public void setResultParam(ArrayList<TwtTaskItem> tasklist,VehicleRoutingProblem problem,
                               VehicleRoutingProblemSolution solution, OsrmTripMatrixResponseParam tripMatrix){
        this.tasklist = tasklist ;
        this.problem = problem ;
        this.solution = solution;
        this.tripMatrix =tripMatrix ;
    }
    public double getDistance(){
        double total_distance = 0;
        for(TwtTaskItem task : tasklist ){
            total_distance += task.tbl_distance;
        }
        return total_distance;
    }
    public double getDuration(){
        double total_duration = 0;
        for(TwtTaskItem task : tasklist ){
            total_duration += task.tbl_duration;
        }
        return total_duration;
    }


}
