package kr.stteam.TwtRoute.service;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.analysis.SolutionAnalyser;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.*;
//import com.graphhopper.jsprit.core.problem.job.Service;

import kr.stteam.TwtRoute.AppProperties;
import kr.stteam.TwtRoute.domain.TwtJobDesc;
import kr.stteam.TwtRoute.domain.TwtTaskItem;
import kr.stteam.TwtRoute.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;


@Service("twtService")
public class TwtService {
    private static Logger logger = LoggerFactory.getLogger(TwtService.class);
    //private static Logger logger = LoggerFactory.getLogger("TSPTW_ROOT");

    private AppProperties appProperties;
    private RouteProc routeProc;

    private TwtTaskItem.taskitem_type jobitem_type;

    @Autowired
    public TwtService(AppProperties appProperties, RouteProcOSRM routeProc) {
        this.appProperties = appProperties;
        this.routeProc = routeProc;
    }


    public ArrayList<TwtTaskItem>  getTaskListFromRequest( TwtRequest_Tsptw request) {
        ArrayList<TwtTaskItem> taskList = new ArrayList<TwtTaskItem>();
        int index = 0;
        String start_task_id = request.getVehicle().getStart_task_id();
        String end_task_id = request.getVehicle().getEnd_task_id();
        for(TwtRequest_Service item : request.getServices() ){
            TwtTaskItem task = new TwtTaskItem();
            task.task_id = item.getTask_id();
            task.task_type = TwtTaskItem.taskitem_type.job_delivery;
            if( start_task_id.compareToIgnoreCase(item.getTask_id()) == 0){
                task.task_type = TwtTaskItem.taskitem_type.job_start;
            }else if( end_task_id != null ){
                if( end_task_id.compareToIgnoreCase(item.getTask_id()) == 0)
                    task.task_type = TwtTaskItem.taskitem_type.job_end;
            }
            task.x = item.getLoc_coord().get(0);
            task.y = item.getLoc_coord().get(1);
            task.tm_service = item.getService_time();
            task.index = index;
            task.poi_name = item.getName();
            if( item.getTime_window() != null)
            {
                // 문자열 parsing 시에 exception throw해야함.
                task.tw_req_start = item.getTime_window().get(0);
                task.tw_req_end =  item.getTime_window().get(1);
                task.tw_req = 1;
            }
            taskList.add(task);
            index ++ ;
        }
        return taskList;
    }

    public TwtResponse_Tsptw procTwt( TwtJobDesc twtJobDesc ) {

        FastVehicleRoutingTransportCostsMatrix costMatrixBuilder = null;
        //TwtJobDesc twtJobDesc = TwtJobDesc.create(reqParam);
        twtJobDesc.proc_start_time = System.currentTimeMillis();
        TwtRequest_Tsptw requestParam = twtJobDesc.requestParam;


        twtJobDesc.debuginfo = twtJobDesc.requestParam.getOptions().isReq_debuginfo();
        ArrayList<TwtTaskItem> tasklist = getTaskListFromRequest(twtJobDesc.requestParam);
        OsrmTripMatrixResponseParam tripMatrix = requestRouteTable(tasklist, twtJobDesc);
        if (tripMatrix == null) {
            return null;
        }
        try {
            costMatrixBuilder = createMatrix(tripMatrix);
        } catch (Exception e) {
            return null;
        }

        /*
         * get a vehicle type-builder and build a type with the typeId "vehicleType" and
         * one capacity dimension, i.e. weight, and capacity dimension value of 2
         */
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType")
            .addCapacityDimension(0, 200).setCostPerWaitingTime(600);// time cost unit

        VehicleType vehicleType = vehicleTypeBuilder.build();


        /*
         * get a vehicle-builder and build a vehicle located at (10,10) with type
         * "vehicleType"
         */
        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");

        Coordinate startpt = Coordinate.newInstance(requestParam.getStartTask().getLoc_coord().get(0), requestParam.getStartTask().getLoc_coord().get(1));
        vehicleBuilder.setStartLocation(Location.Builder.newInstance().setIndex(requestParam.getStartTaskIdx()).setCoordinate(startpt).build());

        //default return to depot = true, endLocation을 등록안해도 , 시작점으로 돌아옴.
        //default return to depot = false로 하면 배송 마지막지점이 종료점
        if( requestParam.getEndTask() == null ) {
            vehicleBuilder.setReturnToDepot(false);
        }else {
            Coordinate endpt = Coordinate.newInstance(requestParam.getEndTask().getLoc_coord().get(0), requestParam.getEndTask().getLoc_coord().get(1));
            vehicleBuilder.setEndLocation(Location.Builder.newInstance().setIndex(requestParam.getEndTaskIdx()).setCoordinate(endpt).build());
        }

        vehicleBuilder.setType(vehicleType);
        vehicleBuilder.setEarliestStart(requestParam.getVehicle().getStart_time());
        vehicleBuilder.setLatestArrival(requestParam.getVehicle().getEnd_time());
        VehicleImpl vehicle = vehicleBuilder.build();

        /*
         * build services at the required locations, each with a capacity-demand of 1.
         */
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle);
//            .addVehicle(vehicle2).addVehicle(vehicle3);
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

        vrpBuilder.setRoutingCost(costMatrixBuilder);

        Random random = RandomNumberGeneration.newInstance();

        for (TwtTaskItem job : tasklist) {
            if (job.task_type == TwtTaskItem.taskitem_type.job_start ||
                job.task_type == TwtTaskItem.taskitem_type.job_end) {
                // 출발지, 목적지는 제외
            } else if (job.tw_req > 0) {
                int rd = random.nextInt(10000);
                com.graphhopper.jsprit.core.problem.job.Service service = com.graphhopper.jsprit.core.problem.job.Service.Builder.newInstance(job.task_id)
                    .addTimeWindow(job.tw_req_start, job.tw_req_end)
                    .addSizeDimension(0, 1)
                    .setServiceTime(job.tm_service)
                    .setLocation(Location.Builder.newInstance().setIndex(job.index).setCoordinate(Coordinate.newInstance(job.x, job.y)).build())
                    .build();
                vrpBuilder.addJob(service);

            } else {
                com.graphhopper.jsprit.core.problem.job.Service service = com.graphhopper.jsprit.core.problem.job.Service.Builder.newInstance(job.task_id)
                    .addSizeDimension(0, 1)
                    .setServiceTime(job.tm_service)
                    .setLocation(Location.Builder.newInstance().setIndex(job.index).setCoordinate(Coordinate.newInstance(job.x, job.y)).build())
                    .build();
                vrpBuilder.addJob(service);
            }

        }

        final VehicleRoutingProblem problem = vrpBuilder.build();
        Jsprit.Builder builder = Jsprit.Builder.newInstance(problem);

        //builder.setProperty(Jsprit.Parameter.ITERATIONS, "10" );
        /*
        if(reqParam.getOptions().getAdmin_max_iteration() > 0 ) {
            builder.setProperty(Jsprit.Parameter.ITERATIONS, reqParam.getOptions().getAdmin_max_iteration().toString());
            logger.info("Admin_max_iteration: ",reqParam.getOptions().getAdmin_max_iteration() );
        }
        if(reqParam.getOptions().getAdmin_thread_cnt() > 0 ) {
            builder.setProperty(Jsprit.Parameter.THREADS, reqParam.getOptions().getAdmin_thread_cnt().toString());
            logger.info("Admin_thread_cnt: ",reqParam.getOptions().getAdmin_thread_cnt() );
        }
        if(reqParam.getOptions().getAdmin_algo_fast_regret() ) {
            builder.setProperty(Jsprit.Parameter.FAST_REGRET, reqParam.getOptions().getAdmin_algo_fast_regret().toString());
            logger.info("Admin_algo_fast_regret: ",reqParam.getOptions().getAdmin_algo_fast_regret() );
        }
        if(reqParam.getOptions().getAdmin_algo_best_insertion() ) {
            builder.setProperty(Jsprit.Parameter.CONSTRUCTION, reqParam.getOptions().getAdmin_algo_best_insertion().toString());
            logger.info("Admin_algo_best_insertion: ",reqParam.getOptions().getAdmin_algo_best_insertion() );
        }
*/
        VehicleRoutingAlgorithm algorithm = builder.buildAlgorithm();

//        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(problem)
//            .setProperty(Jsprit.Parameter.THREADS, "4")
//            .setProperty(Jsprit.Parameter.ITERATIONS, "200")
//            .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
//            .setProperty(Jsprit.Parameter.CONSTRUCTION, Jsprit.Construction.BEST_INSERTION.toString())
//            .buildAlgorithm();

		//VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/algorithmConfig_fix.xml");
        /*
         * and search a solution
         */
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        /*
         * get the best
         */
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        twtJobDesc.setResultParam( problem, bestSolution);


        TwtResponse_Tsptw response = MakeTwtResult(twtJobDesc);
        twtJobDesc.proc_end_time_after_resp_json = System.currentTimeMillis();
        SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);
        SolutionAnalyser a = new SolutionAnalyser(problem, bestSolution, problem.getTransportCosts());

        System.out.println("distance: " + a.getDistance());
        System.out.println("ttime: " + a.getTransportTime());
        System.out.println("completion: " + a.getOperationTime());
        System.out.println("waiting: " + a.getWaitingTime());


        twtJobDesc.proc_total_time_after_resp_json = twtJobDesc.proc_end_time_after_resp_json - twtJobDesc.proc_start_time;
        logger.info("total took {} ms , total= {} ms", twtJobDesc.proc_total_time_before_resp_json,
            twtJobDesc.proc_total_time_after_resp_json);


        //ObjectMapper mapper = new ObjectMapper();
        //twtResult.jsonResult = mapper.writeValueAsString(response);

        return response;
    }

    private FastVehicleRoutingTransportCostsMatrix createMatrix(OsrmTripMatrixResponseParam tripMatrix) {

        FastVehicleRoutingTransportCostsMatrix.Builder builder = FastVehicleRoutingTransportCostsMatrix.Builder
            .newInstance(tripMatrix.getTaskCount(), false);

        int viaCount = tripMatrix.getDurations().size();
        for (int i = 0; i < viaCount; i++) { // start point index
            for (int j = 0; j < viaCount; j++) { // end point index
                builder.addTransportDistance(i, j, tripMatrix.getDistances().get(i)[j]);
                builder.addTransportTime(i, j, tripMatrix.getDurations().get(i)[j]);
            }
        }
        return builder.build();
    }


    /**
     * @param joblist
     * @return
     */
    private OsrmTripMatrixResponseParam requestRouteTable(List<TwtTaskItem> joblist, TwtJobDesc twtJobDesc) {
        StringBuffer buffer = new StringBuffer();

        logger.debug("requestRouteTable");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int task_count = 0;
        // table 구할때는 , 출/목 포함해야한다.
        for (TwtTaskItem job_n : joblist) {
            if (task_count > 0) {
                buffer.append(";");
            }
            buffer.append(String.format("%.7f,%.7f", job_n.x, job_n.y));
            task_count++;
        }

        // osrm 서버 요청.
        //twtResult.setRouteProcess(RouteProcOSRM.create(appProperties));
        String responseJson = routeProc.requestTripMatrix(buffer);
        routeProc.setTripMatrixInResult(responseJson, twtJobDesc);
        OsrmTripMatrixResponseParam tripMatrix = twtJobDesc.getTripMatrix();

        //PrintTripMatrix( tripMatrix );

        stopWatch.stop();
        logger.debug("pre-processing comp-time: {}", stopWatch);
        logger.debug("matrix json:"+responseJson);

        return tripMatrix;
    }

    private void PrintTripMatrix(OsrmTripMatrixResponseParam tripMatrix) {
        StringBuffer costs = new StringBuffer();
        int row = 0;
        for (double[] ar : tripMatrix.getDurations()) {

            costs.append(String.format("[%d] ", row));
            row++;
            for (int k = 0; k < ar.length; k++) {
                costs.append(String.format("%.1f, ", ar[k]));
            }
            logger.info(costs.toString());
            costs.setLength(0); // string buffer initialize
        }
    }


    /**
     * 서버에서 tsp solution 계산이 끝난 후에 , client 에 보내기 위한 result 를 만든다.
     * twtResult.twtResponse 에 최종결과물을 생성하고,return 한다.
     * @param twtJobDesc
     * @return
     */
    TwtResponse_Tsptw MakeTwtResult(TwtJobDesc twtJobDesc) {
        try {
            twtJobDesc.twtResponseTsptw = new TwtResponse_Tsptw();
            collectTsptwOutput(twtJobDesc);
            setResultTaskInfo(twtJobDesc); // 대부분 필요한 응답내용은 여기서 채운다.

            if( twtJobDesc.requestParam.getOptions().isReq_route_geom()){
                String responseJson = routeProc.requestRouteGeometry(twtJobDesc);
                routeProc.setRouteGeometryInResult(responseJson, twtJobDesc);// 경로 polyline이 필요한 경우 여기서 입력한다.
            }


            twtJobDesc.proc_end_time_before_resp_json = System.currentTimeMillis();
            twtJobDesc.proc_total_time_before_resp_json = twtJobDesc.proc_end_time_before_resp_json - twtJobDesc.proc_start_time;
            twtJobDesc.twtResponseTsptw.setProcessing_time(twtJobDesc.proc_total_time_before_resp_json);
            return twtJobDesc.twtResponseTsptw;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public String getDeliveryOrder(TwtJobDesc twtJobDesc) {
        StringBuffer viaPoints = new StringBuffer();
        List<VehicleRoute> list = new ArrayList<VehicleRoute>(twtJobDesc.solution.getRoutes());
        Collections.sort(list, new com.graphhopper.jsprit.core.util.VehicleIndexComparator());

        int route_order = 0;

        for (VehicleRoute route : list) {
            double costs = 0;
            Coordinate start = route.getStart().getLocation().getCoordinate();
            viaPoints.append(String.format("%.7f,%.7f;", start.getX(), start.getY()));

            for (TourActivity act : route.getActivities()) {
                Coordinate coord = act.getLocation().getCoordinate();
                viaPoints.append(String.format("%.7f,%.7f;", coord.getX(), coord.getY()));
                route_order++;
            }
            Coordinate end = route.getEnd().getLocation().getCoordinate();
            viaPoints.append(String.format("%.7f,%.7f", end.getX(), end.getY()));

        }
        return viaPoints.toString();
    }

    /**
     *
     * @param twtJobDesc
     * @return
     */
    private TwtResponse_Tsptw setResultTaskInfo(TwtJobDesc twtJobDesc) {

        //response base
        TwtResponse_Solution solution = new TwtResponse_Solution();

        ArrayList<TwtResponse_SolutionRoute> solutionRouteList = new ArrayList<TwtResponse_SolutionRoute>();
        TwtResponse_SolutionRoute solutionRoute = new TwtResponse_SolutionRoute();

        ArrayList<TwtResponse_RouteActivity> routeActivityList = new ArrayList<TwtResponse_RouteActivity>();
        int order = 0;
        for (TwtTaskItem task : twtJobDesc.tasklist) {
            TwtResponse_RouteActivity activity = new TwtResponse_RouteActivity();
            activity.setTask_id(task.task_id);
            activity.setLoc_coord(new double[]{task.x, task.y});
            activity.setLoc_name(task.poi_name);
            activity.setDistance(task.tbl_distance);
            activity.setDuration(task.tbl_duration);
            activity.setReq_task_index(task.index);
            activity.setArrival_time(task.tm_arrival);
            activity.setEnd_time(task.tm_end);
            if (task.task_type ==  TwtTaskItem.taskitem_type.job_start ) {
                activity.setTask_type("start");
            }
            else if (task.task_type ==  TwtTaskItem.taskitem_type.job_end ) {
                activity.setTask_type("end");
            }
            else if (task.task_type ==  TwtTaskItem.taskitem_type.job_delivery ) {
                activity.setTask_type("delivery");
            }
            else {// (task.task_type ==  TwtTaskItem.taskitem_type.job_unassigned )
            }

            activity.setTask_type("");

            if(task.tw_req_end > 0 ){

//                ArrayList<String> tw = new  ArrayList<String>();
//                tw.add(UtilCommon.convSectoHM(task.tw_req_start));
//                tw.add(UtilCommon.convSectoHM(task.tw_req_end));
//                activity.setTime_window(tw);
                double[] tws = new double[]{task.tw_req_start, task.tw_req_end};
                activity.setTime_window(tws);
            }

            activity.setTask_order(order);

            routeActivityList.add(activity);
            order++;
        }

        solutionRoute.setActivities(routeActivityList);
        solutionRouteList.add(solutionRoute);
        solution.setRoutes(solutionRouteList);


        if(twtJobDesc.unassigned_task != null ){
            solution.setUnassigned(twtJobDesc.unassigned_task);
        }


        //2021-07-06 job id는 request 시 job assign 하면서 할당되도록 수정함
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());
        //twtResult.twtResponse.setJob_id("job-" + timeStamp);
        twtJobDesc.twtResponseTsptw.setJob_id(twtJobDesc.requestParam.getJobId()); //request의 job-id를 사용하도록 수정
        twtJobDesc.twtResponseTsptw.setStatus(TwtResponse_Base.StatusType.Ok);
        twtJobDesc.twtResponseTsptw.setSolution(solution);




        return twtJobDesc.twtResponseTsptw;
        //response base-solution

    }

    /**
     * jsprit solution, 요청데이터, 중간데이터 등을 이용해서 산출물을 병합하다.
     * 여기서 response json에 들어가는 값을 입력하지 않는다. 산재한 TSP 결과물을 머지하는 작업을 한다.
     * 방문순서에 따라 , twtResult.tasklist에 입력한다.
     * 할당되지 않은 task는 twtResult.unassigned_task 에 입력한다.
     * @param twtJobDesc
     */
    public void collectTsptwOutput(TwtJobDesc twtJobDesc) {

        List<VehicleRoute> tripRouteList = new ArrayList<VehicleRoute>(twtJobDesc.solution.getRoutes());
        Collections.sort(tripRouteList, new com.graphhopper.jsprit.core.util.VehicleIndexComparator());
        int tw_count = 0, tw_fail_count = 0;
        int visit_order = 0;
        TwtTaskItem task_rst = null;
        OsrmTripMatrixResponseParam tripMatrix = twtJobDesc.getTripMatrix();
        double route_distance = 0;
        double route_duration = 0;
        double route_transport_time = 0;

        ArrayList<TwtTaskItem> orderedTasklist = new ArrayList<TwtTaskItem>();

        for (VehicleRoute route : tripRouteList) {
            double costs = 0;

            TourActivity prevAct = route.getStart();
            // start pos

            task_rst = new TwtTaskItem();
            //task_rst.index = route.getStart().getIndex();
            task_rst.index = route.getStart().getLocation().getIndex();
            task_rst.order = visit_order; // Integer.parseInt(array[1]);
            task_rst.x = route.getStart().getLocation().getCoordinate().getX();
            task_rst.y = route.getStart().getLocation().getCoordinate().getY();

            task_rst.tm_end = (int) Math.round(route.getStart().getEndTime());
            task_rst.tm_arrival = (int) Math.round(route.getStart().getArrTime());
            //task_rst.task_id = route.getStart().getName();
            task_rst.task_id = twtJobDesc.requestParam.getStartTask().getTask_id();
            task_rst.task_type = TwtTaskItem.taskitem_type.job_start;
            task_rst.poi_name = twtJobDesc.requestParam.getServices().get(task_rst.index).getName();


            visit_order++;
            orderedTasklist.add(task_rst);

            for (TourActivity act : route.getActivities()) {
                String jobId;
                if (act instanceof TourActivity.JobActivity) {
                    jobId = ((TourActivity.JobActivity) act).getJob().getId();
                } else {
                    jobId = "-";
                }
                double c = twtJobDesc.problem.getTransportCosts().getTransportCost(prevAct.getLocation(), act.getLocation(),
                    prevAct.getEndTime(), route.getDriver(), route.getVehicle());
                c += twtJobDesc.problem.getActivityCosts().getActivityCost(act, act.getArrTime(), route.getDriver(),
                    route.getVehicle());
                costs += c;

                task_rst = new TwtTaskItem();

                task_rst.tw_req_start = act.getTheoreticalEarliestOperationStartTime();
                task_rst.tw_req_end = act.getTheoreticalLatestOperationStartTime();
                if (task_rst.tw_req_start == 0 && task_rst.tw_req_end == Double.MAX_VALUE)
                    task_rst.tw_req_end = 0;

                task_rst.tm_end = act.getEndTime();
                task_rst.tm_arrival = act.getArrTime();
                task_rst.task_id = jobId;

                task_rst.index = act.getLocation().getIndex();
                task_rst.order = visit_order; // Integer.parseInt(array[1]);
                task_rst.x = act.getLocation().getCoordinate().getX();
                task_rst.y = act.getLocation().getCoordinate().getY();

                task_rst.tm_end = act.getEndTime();
                task_rst.tm_arrival = act.getArrTime();
                task_rst.sum_cost = costs;
                task_rst.last_cost = c;

                if (prevAct.getIndex() == -1) {
                    task_rst.tbl_duration = tripMatrix.getDurations().get(prevAct.getLocation().getIndex())[act.getIndex()];
                    task_rst.tbl_distance = tripMatrix.getDistances().get(prevAct.getLocation().getIndex())[act.getIndex()];
                } else {
                    task_rst.tbl_duration = tripMatrix.getDurations().get(prevAct.getIndex())[act.getIndex()];
                    task_rst.tbl_distance = tripMatrix.getDistances().get(prevAct.getIndex())[act.getIndex()];
                }
                //job_rst.tw_req_start = problem.

                task_rst.task_type = jobitem_type.job_delivery;
                task_rst.poi_name = twtJobDesc.requestParam.getServices().get(task_rst.index).getName();

                orderedTasklist.add(task_rst);

                visit_order++;
                prevAct = act;
            }
            double c = twtJobDesc.problem.getTransportCosts().getTransportCost(prevAct.getLocation(), route.getEnd().getLocation(),
                prevAct.getEndTime(), route.getDriver(), route.getVehicle());
            c += twtJobDesc.problem.getActivityCosts().getActivityCost(route.getEnd(), route.getEnd().getArrTime(),
                route.getDriver(), route.getVehicle());
            costs += c;

            task_rst = new TwtTaskItem();
            //task_rst.index = route.getEnd().getIndex();
            task_rst.index = route.getEnd().getLocation().getIndex();
            task_rst.order = visit_order; // Integer.parseInt(array[1]);
            task_rst.x = route.getEnd().getLocation().getCoordinate().getX();
            task_rst.y = route.getEnd().getLocation().getCoordinate().getY();

            task_rst.tm_end = route.getEnd().getEndTime();
            task_rst.tm_arrival = route.getEnd().getArrTime();
            //task_rst.task_id = route.getEnd().getName();
            if( twtJobDesc.requestParam.getEndTask() != null) {
                task_rst.task_id = twtJobDesc.requestParam.getEndTask().getTask_id();
            }

            task_rst.sum_cost = costs;
            task_rst.last_cost = c;
            task_rst.tbl_duration = tripMatrix.getDurations().get(prevAct.getIndex())[route.getEnd().getLocation().getIndex()];
            task_rst.tbl_distance = tripMatrix.getDistances().get(prevAct.getIndex())[route.getEnd().getLocation().getIndex()];
            //job_rst.last_distance = ;
            task_rst.task_type = jobitem_type.job_end;
            task_rst.poi_name = twtJobDesc.requestParam.getServices().get(task_rst.index).getName();

            visit_order++;
            orderedTasklist.add(task_rst);

        }
        // *** 각 배달지점정보, 배달지점 순서에 따라 저장함.
        twtJobDesc.setTasklist(orderedTasklist);


        // unassigned job이 있으면 여기에서 저장함. 결국
        if (!twtJobDesc.solution.getUnassignedJobs().isEmpty()) {
            twtJobDesc.unassigned_task = new ArrayList<TwtResponse_SolutionUnassigned>();
            for (Job j : twtJobDesc.solution.getUnassignedJobs()) {
                TwtResponse_SolutionUnassigned unassigned = new TwtResponse_SolutionUnassigned();
                unassigned.setTask_id(j.getId());
                unassigned.setReq_task_index(j.getIndex());
                if( j.getActivities().get(0).getTimeWindows().size() > 0) {//if there is time window in task
                    TimeWindow tw = j.getActivities().get(0).getTimeWindows().iterator().next();
                    if (tw.getStart() != 0 || tw.getEnd() != Double.MAX_VALUE){
                        unassigned.setTime_window(new double[]{tw.getStart(), tw.getEnd()});
                    }
                }
                twtJobDesc.unassigned_task.add(unassigned);
            }
        }

        // ------------------
        logger.debug("tw count = {}, fail={}", tw_count, tw_fail_count);
        if (twtJobDesc.unassigned_task != null) logger.debug(" unassigned={}", twtJobDesc.unassigned_task.size());
    }
}
