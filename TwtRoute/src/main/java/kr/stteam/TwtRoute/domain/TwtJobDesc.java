package kr.stteam.TwtRoute.domain;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import kr.stteam.TwtRoute.protocol.*;
import kr.stteam.TwtRoute.protocol.OsrmTripMatrixResponseParam;
import kr.stteam.TwtRoute.util.UtilCommon;

import java.util.ArrayList;

public class TwtJobDesc {

    // client -> server request parameter (json 내용을 class 구조로 받은 것 )
    public TwtRequest_Tsptw requestParam = null;

    // tasklist는 방문순서로 ordering 되어 있음,
    // request 할 때 tasklist와 다름
    public ArrayList<TwtTaskItem> tasklist = null;

    // tsp를 계산하기 위해 , 각 경유지간의 duration table 결과
    private OsrmTripMatrixResponseParam tripMatrix = null;

    // jsprit 계산, 중간결과물
    public VehicleRoutingProblem problem= null;
    // jsprit 계산, 최종결과물
    public VehicleRoutingProblemSolution solution= null;

    // unassigned task list
    public ArrayList<TwtResponse_SolutionUnassigned> unassigned_task = null;

    // osrm table 요청결과 응답내용 ( json 내용을 class 구조로 받은 것)
    public OsrmRouteResponseParam_Base osrmRouteResponse = null;

    // tsptw 서버에서 client로 전달할 결과(추후  json으로 변환되어 전달됨)
    public TwtResponse_Tsptw twtResponseTsptw;

    public TwtJobStopwatch jobStopwatch = new TwtJobStopwatch();

    // debuginfo 정보를 포함할 지 여부, client request param에 있는 필드를 복사해 옴
    public boolean debuginfo = false;

    public double   proc_start_time = 0;
    public double   proc_total_time_before_resp_json =0;
    public double   proc_total_time_after_resp_json =0;
    public double   proc_end_time_before_resp_json = 0;
    public double   proc_end_time_after_resp_json = 0;
    public double   proc_getmatrix_time =0;


    private TwtJobDesc(TwtRequest_Tsptw requestParam) {
        this.requestParam = requestParam;
    }

//    public static TwtJobDesc create(TwtRequest_Tsptw requestParam) {
//        return new TwtJobDesc(requestParam);
//    }
    public static TwtJobDesc create(String requestJson) {
        TwtRequest_Tsptw request = TwtRequestMapper.parseParam(requestJson);

        request.setJobId(UtilCommon.defineJobId()); //20210708 현재시간 기준 jobId 할당.

        return new TwtJobDesc(request);
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
