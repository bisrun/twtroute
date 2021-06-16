package kr.stteam.TwtRoute.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.analysis.SolutionAnalyser;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.*;
//import com.graphhopper.jsprit.core.problem.job.Service;

import kr.stteam.TwtRoute.AppProperties;
import kr.stteam.TwtRoute.controller.TwtResult;
import kr.stteam.TwtRoute.domain.TwtTaskItem;
import kr.stteam.TwtRoute.protocol.*;
import kr.stteam.TwtRoute.util.UtilCommon;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TwtService {
    private static Logger logger = LoggerFactory.getLogger(TwtService.class);
    private AppProperties appProperties;
    private RouteProc routeProc = null;

    //List<TwtJobItem> joblist = new ArrayList<TwtJobItem>();
    //List<TwtTaskItem> routeResultList = new ArrayList<TwtTaskItem>();
    //List<TwtTaskItem> jobUnassignedList = new ArrayList<TwtTaskItem>();


    String VER_524_PORTNO = new String("20000");
    String hostname = new String("192.168.6.45:20000");
    private TwtTaskItem.taskitem_type jobitem_type;

    @Autowired
    public TwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    //String hostname = new String("192.168.6.45:5400");

    public TwtResponseParam_Base procTwt(ArrayList<TwtTaskItem> tasklist, TwtRequestParam_BaseData reqParam) {


        double tmtag_start = System.currentTimeMillis();
        FastVehicleRoutingTransportCostsMatrix costMatrixBuilder = null;
        TwtResult twtResult = TwtResult.create(reqParam);

        OsrmTripMatrixResponseParam tripMatrix = requestRouteTable(tasklist, twtResult);
        if (tripMatrix == null) {
            return null;
        }
        //tripMatrix.setTaskCount(tasklist.size());

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

        Coordinate startpt = Coordinate.newInstance(tasklist.get(0).x, tasklist.get(0).y);
        vehicleBuilder.setStartLocation(Location.Builder.newInstance().setIndex(0).setCoordinate(startpt).build());

        //default return to depot = true, endLocation을 등록안해도 , 시작점으로 돌아옴.
        //default return to depot = false로 하면 배송 마지막지점이 종료점
        //vehicleBuilder.setReturnToDepot(false);
        vehicleBuilder.setEndLocation(Location.Builder.newInstance().setIndex(0).setCoordinate(startpt).build());

        ;
        vehicleBuilder.setType(vehicleType);
        vehicleBuilder.setEarliestStart(UtilCommon.convHMtoSec(reqParam.getVehicle().getStarttime()));
        vehicleBuilder.setLatestArrival(UtilCommon.convHMtoSec(reqParam.getVehicle().getEndtime()));
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
            if (job.index == 0) {
                // 출발지, 목적지는 제외
            } else if (job.tw_req > 0) {
                int rd = random.nextInt(10000);
                com.graphhopper.jsprit.core.problem.job.Service service = com.graphhopper.jsprit.core.problem.job.Service.Builder.newInstance("TW-" + (job.task_id))
                    .addTimeWindow(job.tw_req_start, job.tw_req_end)
                    .addSizeDimension(0, 1)
                    .setServiceTime(job.tm_service)
                    .setLocation(Location.Builder.newInstance().setIndex(job.index).setCoordinate(Coordinate.newInstance(job.x, job.y)).build())
                    .build();
                vrpBuilder.addJob(service);

            } else {
                com.graphhopper.jsprit.core.problem.job.Service service = com.graphhopper.jsprit.core.problem.job.Service.Builder.newInstance("NM-" + (job.task_id))
                    .addSizeDimension(0, 1)
                    .setServiceTime(job.tm_service)
                    .setLocation(Location.Builder.newInstance().setIndex(job.index).setCoordinate(Coordinate.newInstance(job.x, job.y)).build())
                    .build();
                vrpBuilder.addJob(service);
            }

        }

        final VehicleRoutingProblem problem = vrpBuilder.build();

        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(problem).buildAlgorithm();

        /*
         * and search a solution
         */
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        /*
         * get the best
         */
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        twtResult.setResultParam( problem, bestSolution);


        TwtResponseParam_Base response = MakeTwtResult(twtResult);

        double tmtag_end = System.currentTimeMillis();
        SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);
        SolutionAnalyser a = new SolutionAnalyser(problem, bestSolution, problem.getTransportCosts());

        System.out.println("distance: " + a.getDistance());
        System.out.println("ttime: " + a.getTransportTime());
        System.out.println("completion: " + a.getOperationTime());
        System.out.println("waiting: " + a.getWaitingTime());

        logger.info("total took {} seconds", ((tmtag_end - tmtag_start) / 1000.0));


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
    private OsrmTripMatrixResponseParam requestRouteTable(List<TwtTaskItem> joblist, TwtResult twtResult) {
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
        twtResult.setRouteProcess(RouteProcOSRM.create(appProperties));
        String responseJson = twtResult.getRouteProcess().requestTripMatrix(buffer);
        twtResult.getRouteProcess().setTripMatrixInResult(responseJson, twtResult);
        OsrmTripMatrixResponseParam tripMatrix =twtResult.getTripMatrix();

        PrintTripMatrix( tripMatrix );

        stopWatch.stop();
        logger.debug("pre-processing comp-time: {}", stopWatch);

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


    TwtResponseParam_Base MakeTwtResult(TwtResult twtResult) {
        try {
            twtResult.twtResponse = new TwtResponseParam_Base();
            setRouteResult(twtResult);
            setResultObject(twtResult);

            String responseJson = twtResult.getRouteProcess().requestRouteGeometry(twtResult);
            twtResult.getRouteProcess().setRouteGeometryInResult(responseJson, twtResult);

            //setRouteGeometry(twtResult);
            return twtResult.twtResponse;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public String getDeliveryOrder(TwtResult twtResult) {
        StringBuffer viaPoints = new StringBuffer();
        List<VehicleRoute> list = new ArrayList<VehicleRoute>(twtResult.solution.getRoutes());
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

    private String setRouteGeometry(TwtResult twtResult) {

        String body = null;
        StringBuffer requestUrl = new StringBuffer("http://" + hostname + "/route/v1/car/");
        requestUrl.append(getDeliveryOrder(twtResult)); // 추후 변경해야함.
        requestUrl.append("?geometries=geojson&overview=full");
        // http://192.168.6.45:5500/route/v1/car/127.1162273,37.5168241;127.1143369,37.5088933;127.1015711,37.5138507?geometries=geojson

        logger.info("url: " + requestUrl);

        try {
            HttpGet httpGet = new HttpGet(requestUrl.toString());
            httpGet.setHeader("Accept", "application/json");

            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            CloseableHttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                ResponseHandler<String> handler = new BasicResponseHandler();
                body = handler.handleResponse(response);
                twtResult.osrmRouteResponse = mapper.readValue(body, OsrmRouteResponseParam_Base.class);
                setGeometryOnResult(twtResult);


                //tripMatrix = mapper.readValue(body, TripResultMatrixOSRM.class);
                // TypeReference<List<BoardModel>> typeReference = new
                // TypeReference<List<BoardModel>>(){};
                // List<BoardModel> boardModelList = objectMapper.readValue(body,
                // typeReference);
                // BoardModel boardModel = objectMapper.readValue(body, BoardModel.class);
                //logger.info("## res boardModel={}", body);
                logger.info("---------ok-----------");
            } else {
                logger.info("response error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("fail to get osrm result !!!");
            return null;
        }

        return body;
    }

    private boolean setGeometryOnResult(TwtResult twtResult) {
        int waypoint_idx = 0;
        int prev_waypoint_idx = 0;
        ArrayList<double[]> coordinates = twtResult.osrmRouteResponse.getRoutes().get(0).getGeometry().getCoordinates();
        ArrayList<OsrmRouteResponseParam_Waypoint> waypoints = twtResult.osrmRouteResponse.getWaypoints();


        for (int vtx_loop = 0; vtx_loop < coordinates.size(); vtx_loop++) {
            if (coordinates.get(vtx_loop)[0] == waypoints.get(waypoint_idx).getLocation()[0] &&
                coordinates.get(vtx_loop)[1] == waypoints.get(waypoint_idx).getLocation()[1] ) {
                waypoints.get(waypoint_idx).setStart_coord_idx(prev_waypoint_idx);
                waypoints.get(waypoint_idx).setCoord_count(vtx_loop - prev_waypoint_idx);
                prev_waypoint_idx = vtx_loop;
                waypoint_idx++;
            }
        }
        int route_order = -1;
        int vtx_index = 0;

        for (OsrmRouteResponseParam_Waypoint wp : waypoints) {
            route_order++;
            if (wp.getCoord_count() < 1) {
                continue;
            }

            ArrayList<double[]> linecoords = new ArrayList<double[]>();
            for (int vtx_loop1 = 0; vtx_loop1 <= wp.getCoord_count() ; vtx_loop1++) {
                vtx_index = vtx_loop1 + wp.getStart_coord_idx();

                //double[] coordnate = new double[]{coordinates.get(vtx_index)[0],coordinates.get(vtx_index)[1]};
                linecoords.add(coordinates.get(vtx_index));
            }

            TripGeometry geometry = new TripGeometry();
            ArrayList<TwtResponseParam_RouteActivite> activities = twtResult.twtResponse.getSolution().getRoutes().get(0).getActivities();

            geometry.setCoordinates(linecoords);
            activities.get(route_order).setGeometry(geometry);
        }
        return true;
    }

    private TwtResponseParam_Base setResultObject(TwtResult twtResult) {

        //response base
        TwtResponseParam_Solution solution = new TwtResponseParam_Solution();
        solution.setCosts(100f);

        ArrayList<TwtResponseParam_SolutionRoute> solutionRouteList = new ArrayList<TwtResponseParam_SolutionRoute>();
        TwtResponseParam_SolutionRoute solutionRoute = new TwtResponseParam_SolutionRoute();
        solutionRoute.setCosts(1000);

        ArrayList<TwtResponseParam_RouteActivite> routeActivityList = new ArrayList<TwtResponseParam_RouteActivite>();
        int order = 0;
        for (TwtTaskItem task : twtResult.tasklist) {
            TwtResponseParam_RouteActivite activity = new TwtResponseParam_RouteActivite();
            activity.setTask_id(task.task_id);
            activity.setPoint(new double[]{task.x, task.y});
            activity.setLoc_name(task.poi_name);
            activity.setDistance(task.tbl_distance);
            activity.setTms_duration(task.tbl_duration);
            activity.setReq_task_index(task.index);
            activity.setTms_arrival(task.tm_arrival);
            activity.setTms_end(task.tm_end);
            activity.setTm_arrival(UtilCommon.convSectoHMS(task.tm_arrival));
            activity.setTm_end(UtilCommon.convSectoHMS(task.tm_end));
            if(task.tw_req_end > 0 ){
                ArrayList<String> tw = new  ArrayList<String>();
                tw.add(UtilCommon.convSectoHM(task.tw_req_start));
                tw.add(UtilCommon.convSectoHM(task.tw_req_end));
                activity.setTimewindow(tw);

                double[] tws = new double[]{task.tw_req_start, task.tw_req_end};
                activity.setTms_timewindow(tws);
            }

            activity.setLoc_name(task.poi_name);
            activity.setTask_order(order);

            routeActivityList.add(activity);
            order++;
        }

        solutionRoute.setActivities(routeActivityList);
        solutionRouteList.add(solutionRoute);
        solution.setRoutes(solutionRouteList);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());
        twtResult.twtResponse.setJob_id("job-" + timeStamp);
        twtResult.twtResponse.setStatus("OK");
        twtResult.twtResponse.setSolution(solution);

        return twtResult.twtResponse;
        //response base-solution

    }

    public void setRouteResult(TwtResult twtResult) {

        List<VehicleRoute> tripRouteList = new ArrayList<VehicleRoute>(twtResult.solution.getRoutes());
        Collections.sort(tripRouteList, new com.graphhopper.jsprit.core.util.VehicleIndexComparator());
        int tw_count = 0, tw_fail_count = 0;
        int visit_order = 0;
        TwtTaskItem task_rst = null;
        OsrmTripMatrixResponseParam tripMatrix = twtResult.getTripMatrix();

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
            task_rst.task_id = route.getStart().getName();
            task_rst.task_type = TwtTaskItem.taskitem_type.job_start;
            task_rst.poi_name = twtResult.requestParam.getServices().get(task_rst.index).getName();


            visit_order++;
            twtResult.tasklist.add(task_rst);

            for (TourActivity act : route.getActivities()) {
                String jobId;
                if (act instanceof TourActivity.JobActivity) {
                    jobId = ((TourActivity.JobActivity) act).getJob().getId();
                } else {
                    jobId = "-";
                }
                double c = twtResult.problem.getTransportCosts().getTransportCost(prevAct.getLocation(), act.getLocation(),
                    prevAct.getEndTime(), route.getDriver(), route.getVehicle());
                c += twtResult.problem.getActivityCosts().getActivityCost(act, act.getArrTime(), route.getDriver(),
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

                task_rst.index = act.getIndex();
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
                task_rst.poi_name = twtResult.requestParam.getServices().get(task_rst.index).getName();

                twtResult.tasklist.add(task_rst);

                visit_order++;
                prevAct = act;
            }
            double c = twtResult.problem.getTransportCosts().getTransportCost(prevAct.getLocation(), route.getEnd().getLocation(),
                prevAct.getEndTime(), route.getDriver(), route.getVehicle());
            c += twtResult.problem.getActivityCosts().getActivityCost(route.getEnd(), route.getEnd().getArrTime(),
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
            task_rst.task_id = route.getEnd().getName();

            task_rst.sum_cost = costs;
            task_rst.last_cost = c;
            task_rst.tbl_duration = tripMatrix.getDurations().get(prevAct.getIndex())[route.getEnd().getLocation().getIndex()];
            task_rst.tbl_distance = tripMatrix.getDistances().get(prevAct.getIndex())[route.getEnd().getLocation().getIndex()];
            //job_rst.last_distance = ;
            task_rst.task_type = jobitem_type.job_end;
            task_rst.poi_name = twtResult.requestParam.getServices().get(task_rst.index).getName();

            visit_order++;
            orderedTasklist.add(task_rst);

        }
        twtResult.setTasklist(orderedTasklist);


        if (!twtResult.solution.getUnassignedJobs().isEmpty()) {
            twtResult.unassigned_task = new ArrayList<TwtResponseParam_SolutionUnassigned>();
            for (Job j : twtResult.solution.getUnassignedJobs()) {
                TwtResponseParam_SolutionUnassigned unassigned = new TwtResponseParam_SolutionUnassigned();
                unassigned.setTask_id(j.getId());
                unassigned.setReq_task_index(j.getIndex());

                twtResult.unassigned_task.add(unassigned);
            }
        }

        // ------------------
        logger.debug("tw count = {}, fail={}", tw_count, tw_fail_count);
        if (twtResult.unassigned_task != null) logger.debug(" unassigned={}", twtResult.unassigned_task.size());
    }
}
