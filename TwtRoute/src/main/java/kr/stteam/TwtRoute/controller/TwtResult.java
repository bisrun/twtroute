package kr.stteam.TwtRoute.controller;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.Coordinate;
import kr.stteam.TwtRoute.protocol.*;
import kr.stteam.TwtRoute.protocol.OsrmTripMatrixResponseParam;
import kr.stteam.TwtRoute.domain.TwtTaskItem;
import kr.stteam.TwtRoute.service.RouteProc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TwtResult {
    public TwtRequestParam_BaseData requestParam = null;


    // tasklist는 방문순서로 ordering 되어 있음,
    // request 할 때 tasklist와 다름. ordering 안되어 있음.
    public ArrayList<TwtTaskItem> tasklist = null;

    private OsrmTripMatrixResponseParam tripMatrix = null;
    public VehicleRoutingProblem problem= null;
    public VehicleRoutingProblemSolution solution= null;
    public ArrayList<TwtResponseParam_SolutionUnassigned> unassigned_task = null;
    public OsrmRouteResponseParam_Base osrmRouteResponse = null;
    public TwtResponseParam_Base twtResponse;
    public String jsonResult;

    RouteProc routeProcess;

    public RouteProc getRouteProcess() {
        return routeProcess;
    }

    public void setRouteProcess(RouteProc routeProcess) {
        this.routeProcess = routeProcess;
    }

    private TwtResult(TwtRequestParam_BaseData requestParam) {
        this.requestParam = requestParam;
    }

    public static TwtResult create(TwtRequestParam_BaseData requestParam) {
        return new TwtResult(requestParam);
    }
    public OsrmTripMatrixResponseParam getTripMatrix() {
        return tripMatrix;
    }

    public void setTripMatrix(OsrmTripMatrixResponseParam tripMatrix) {
        this.tripMatrix = tripMatrix;
    }

    public void setTasklist(ArrayList<TwtTaskItem> tasklist) {
        this.tasklist = tasklist;
    }

    public void setResultParam(VehicleRoutingProblem problem,
                               VehicleRoutingProblemSolution solution){
        this.problem = problem ;
        this.solution = solution;
        //this.tripMatrix =tripMatrix ;
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
    public String GetOrderedWaypoint(){
        StringBuffer viaPoints = new StringBuffer();
        int route_order = 0;
        for (TwtTaskItem item : tasklist ) {
            if( route_order > 0 ){
                viaPoints.append(String.format(";%.7f,%.7f", item.x, item.y));
            } else {
                viaPoints.append(String.format("%.7f,%.7f", item.x, item.y));
            }
            route_order++;
        }
        return viaPoints.toString();
    }
}
