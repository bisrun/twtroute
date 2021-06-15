/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.examples;

import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.recreate.BestInsertionConcurrent;
import com.graphhopper.jsprit.core.analysis.SolutionAnalyser;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl.Builder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.ManhattanCosts;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.StopWatch;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.FastVehicleRoutingTransportCostsMatrix;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

//
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

///



import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;

import java.lang.reflect.Type;

//
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
//import org.opengis.geometry.coordinate.GeometryFactory;
//import org.opengis.geometry.coordinate.LineString;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.dbf.DbaseFileWriter;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileReader.Record;
import org.geotools.data.shapefile.shp.ShapefileWriter;

//
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//

public class MultipleTimeWindowExample3_3 {
	private static Logger logger = LoggerFactory.getLogger(MultipleTimeWindowExample3_3.class);

	List<job_item> joblist = new ArrayList<job_item>();
	List<job_item> routeResultList = new ArrayList<job_item>();
	List<job_item> jobUnassignedList = new ArrayList<job_item>();
	TripMatrixOSRM tripMatrixforDebug = null;

	String VER_524_PORTNO = new String("20000");
	String hostname = new String("192.168.6.45:20000");
	//String hostname = new String("192.168.6.45:5400");

	String filepathRequestJob = "D:/download/viapoints010.txt";

	public static void main(String[] args) {


		MultipleTimeWindowExample3_3 mtwex3 = new MultipleTimeWindowExample3_3();
		FastVehicleRoutingTransportCostsMatrix costMatrixBuilder = null;

		double tmtag_start = System.currentTimeMillis();
		try {
			costMatrixBuilder = mtwex3.createMatrix();
		} catch (Exception e) {
			return;
		}

		/*
		 * get a vehicle type-builder and build a type with the typeId "vehicleType" and
		 * one capacity dimension, i.e. weight, and capacity dimension value of 2
		 */
		VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType")
				.addCapacityDimension(0, 200).setCostPerWaitingTime(600)// time cost unit

		;
		VehicleType vehicleType = vehicleTypeBuilder.build();

		/*
		 * get a vehicle-builder and build a vehicle located at (10,10) with type
		 * "vehicleType"
		 */
		Builder vehicleBuilder = Builder.newInstance("vehicle");

		Coordinate startpt = Coordinate.newInstance(mtwex3.joblist.get(0).x, mtwex3.joblist.get(0).y);
		vehicleBuilder.setStartLocation(Location.Builder.newInstance().setIndex(0).setCoordinate(startpt).build());

		//default return to depot = true, endLocation을 등록안해도 , 시작점으로 돌아옴.
		//default return to depot = false로 하면 배송 마지막지점이 종료점
		//vehicleBuilder.setReturnToDepot(false);
		vehicleBuilder.setEndLocation(Location.Builder.newInstance().setIndex(0).setCoordinate(startpt).build());

		vehicleBuilder.setType(vehicleType);
		//vehicleBuilder.setEarliestStart(mtwex3.convHMtoSec("09:00"));
		vehicleBuilder.setLatestArrival(100000);
		VehicleImpl vehicle = vehicleBuilder.build();

//        Builder vehicleBuilder2 = Builder.newInstance("vehicle2");
//        vehicleBuilder2.setStartLocation(Location.newInstance(0, 0));
//        vehicleBuilder2.setType(vehicleType);
//        vehicleBuilder2.setEarliestStart(250).setLatestArrival(450);
//        VehicleImpl vehicle2 = vehicleBuilder2.build();
//
//
//        Builder vehicleBuilder3 = Builder.newInstance("vehicle3");
//        vehicleBuilder3.setStartLocation(Location.newInstance(0, 0));
//        vehicleBuilder3.setType(vehicleType);
//        vehicleBuilder3.setEarliestStart(380).setLatestArrival(600);
//        VehicleImpl vehicle3 = vehicleBuilder3.build();

		/*
		 * build services at the required locations, each with a capacity-demand of 1.
		 */
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(vehicle);
//            .addVehicle(vehicle2).addVehicle(vehicle3);
		vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

		vrpBuilder.setRoutingCost(costMatrixBuilder);

		Random random = RandomNumberGeneration.newInstance();

		for (job_item job : mtwex3.joblist) {
			if (job.index == 0) {
				// 출발지, 목적지는 제외
			} else if (job.tw_req > 0 ) {
				int rd = random.nextInt(10000);
				Service service = Service.Builder.newInstance("TW-" + (job.index))
						.addTimeWindow(job.tw_req_start, job.tw_req_end)
						.addSizeDimension(0, 1)
						.setServiceTime(60)
						.setLocation(Location.Builder.newInstance().setIndex(job.index).setCoordinate(Coordinate.newInstance(job.x, job.y)).build())
						.build();
				vrpBuilder.addJob(service);

			} else {
				Service service = Service.Builder.newInstance("NM-" + (job.index))
						.addSizeDimension(0, 1)
						.setServiceTime(60)
						.setLocation(Location.Builder.newInstance().setIndex(job.index).setCoordinate(Coordinate.newInstance(job.x, job.y)).build())
						.build();
				vrpBuilder.addJob(service);
			}

		}
//
//        for(int i=0;i<40;i++){
//            Service service = Service.Builder.newInstance("" + (i + 1))
////                .addTimeWindow(random.nextInt(50), 200)
////                .addTimeWindow(220 + random.nextInt(50), 350)
////                .addTimeWindow(400 + random.nextInt(50), 550)
////                .addSizeDimension(0, 1)
//                .setServiceTime(1)
//                .setLocation(Location.newInstance(random.nextInt(50), random.nextInt(50))).build();
//            vrpBuilder.addJob(service);
//        }
//
//        for(int i=0;i<12;i++){
//            Service service = Service.Builder.newInstance(""+(i+51))
////                .addTimeWindow(0, 80)
//////                .addTimeWindow(120, 200)
////                .addTimeWindow(250,500)
////                .addSizeDimension(0, 1)
//                .setServiceTime(2)
//                .setLocation(Location.newInstance(50 + random.nextInt(20), 20 + random.nextInt(25))).build();
//            vrpBuilder.addJob(service);
//        }
//
//        Service service = Service.Builder.newInstance("100")
//            .addTimeWindow(50, 80)
//            .setServiceTime(10)
//            .setLocation(Location.newInstance(40, 1)).build();
//        vrpBuilder.addJob(service);

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

		double tmtag_end = System.currentTimeMillis();

		// new VrpXMLWriter(problem,
		// solutions).write("output/problem-with-solution.xml");

		SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);

		mtwex3.MakeResultShape(problem, bestSolution);

		/*
		 * plot
		 */
		new Plotter(problem, bestSolution).setLabel(Plotter.Label.ID).plot("output/plot", "mtw");

		SolutionAnalyser a = new SolutionAnalyser(problem, bestSolution, problem.getTransportCosts());

		System.out.println("distance: " + a.getDistance());
		System.out.println("ttime: " + a.getTransportTime());
		System.out.println("completion: " + a.getOperationTime());
		System.out.println("waiting: " + a.getWaitingTime());

		logger.info("total took {} seconds", ((tmtag_end - tmtag_start) / 1000.0));

//        new GraphStreamViewer(problem, bestSolution).labelWith(Label.ID).setRenderDelay(200).display();
	}

	public enum jobitem_type {
		job_start(0x0001), job_end(0x0002), job_delivery(0x0004), job_unassigned(0x0008);

		private int value;

		jobitem_type(int value) {
			this.value = value;
		}
	};

	public class job_item {
		String job_id;
		double x;
		double y;
		int index; // input index
		String poi_name;
		int order; // job order, input order after processing.

		double mx;
		double my;
		int tw_req;
		double tw_req_start;
		double tw_req_end;
		double tw_end;
		double tm_arrival;
		double sum_cost;
		double tm_end;
		double tm_last_transfer;
		double last_cost;
		double tm_service;
		double tbl_distance;
		double tbl_duration;
		double last_route_distance;
		double last_route_duration;
		double last_route_weight;
		jobitem_type job_type;

		@Override
		public String toString() {
			return String.format("[%d] %d (%.7f, %.7f)", index, order, x, y);
		}

	}

	public boolean load_jobfile(List<job_item> joblist, String filepath, boolean skipHeader) {
		BufferedReader br = null;
		int line_count = 0;

		// filepath file의 1st column 은 입력순서이고 반드시 0 부터 시작하고, sequential하다.
		// 추후 이 column의 값은, 배열의 index로 사용한다.

		try {
			br = Files.newBufferedReader(Paths.get(filepath));
			// Charset.forName("UTF-8");
			String line = "";

			while ((line = br.readLine()) != null) {

				line_count++;
				if (skipHeader && line_count == 1) {
					continue;
				}
				// CSV 1행을 저장하는 리스트
				String array[] = line.split("\t");

				// if(array.length < 5 )
				// continue ;
				job_item job = new job_item();

				// 배열에서 리스트 반환
				job.index = Integer.parseInt(array[0]);// 중요, 0부터 시작, sequential.
				job.poi_name = new String(array[2]);
				job.y = Double.parseDouble(array[3]);
				job.x = Double.parseDouble(array[4]);
				if( array.length == 8)
				{
					job.tw_req =  Integer.parseInt(array[5]) ;
					if(job.tw_req > 0 )
					{
						job.tw_req_start = Double.parseDouble(array[6]);
						job.tw_req_end = Double.parseDouble(array[7]);
					}

				}
				else
				{
					job.tw_req = 0;
					job.tw_req_start =0f;
					job.tw_req_end =0f;
				}

				//logger.info(job.toString());
				joblist.add(job);

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return true;
	}

	private TripMatrixOSRM requestRouteTable(List<job_item> joblist) {
		StringBuffer buffer = new StringBuffer();

		logger.debug("requestRouteTable");
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		int job_count = 0;

		// table 구할때는 , 출/목 포함해야한다.
		for (job_item job_n : joblist) {
			if (job_count > 0) {
				buffer.append(";");
			}
			buffer.append(String.format("%.7f,%.7f", job_n.x, job_n.y));

			job_count++;
		}

		TripMatrixOSRM tripMatrix = getTripMatrix(buffer);
		tripMatrixforDebug = tripMatrix ;
		StringBuffer costs = new StringBuffer();
		int row = 0;

		for (double[] ar : tripMatrix.durations) {

			costs.append(String.format("[%d] ", row));
			row++;
			for (int k = 0; k < ar.length; k++) {
				costs.append(String.format("%.1f, ", ar[k]));
			}
			logger.info(costs.toString());
			costs.setLength(0);
		}
		stopWatch.stop();
		logger.debug("pre-processing comp-time: {}", stopWatch);

		return tripMatrix;
	}

	public class TripMatrixOSRM {
		String code;
		private ArrayList<double[]> durations = new ArrayList<double[]>();
		private ArrayList<double[]> distances = new ArrayList<double[]>();
		// private ArrayList<HashMap<String,Object>> sources= new
		// ArrayList<HashMap<String,Object>>();
		// private Type sources= new TypeToken<ArrayList<HashMap<String, Object>>>()
		// {}.getType();
	}

	private TripMatrixOSRM getTripMatrix(StringBuffer jobPoint) {
		TripMatrixOSRM tripMatrix = null;
		StringBuffer requestUrl = new StringBuffer("http://"+hostname+"/table/v1/car/");
		requestUrl.append(jobPoint);
		if( hostname.contains(VER_524_PORTNO)) // osrm 5.24
		{
			requestUrl.append("?annotations=distance,duration");
		}


		logger.info("url: " + requestUrl);

		try {
			HttpGet httpGet = new HttpGet(requestUrl.toString());
			httpGet.setHeader("Accept", "application/json");

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			CloseableHttpResponse response = httpClient.execute(httpGet);

			if (response.getStatusLine().getStatusCode() == 200) {

				Gson gson = new Gson();

				ResponseHandler<String> handler = new BasicResponseHandler();
				String body = handler.handleResponse(response);
				tripMatrix = gson.fromJson(body, TripMatrixOSRM.class);

				if( !hostname.contains(VER_524_PORTNO)) // osrm 5.24
				{
					tripMatrix.distances =  new ArrayList<double[]>();
				}

				if(tripMatrix.distances.size() == 0 )
				{

					for( double[] ar : tripMatrix.durations) {
						double[] arDist = ar.clone();
						tripMatrix.distances.add(arDist); // distance가 없어서,
					}
				}

				// TypeReference<List<BoardModel>> typeReference = new
				// TypeReference<List<BoardModel>>(){};
				// List<BoardModel> boardModelList = objectMapper.readValue(body,
				// typeReference);
				// BoardModel boardModel = objectMapper.readValue(body, BoardModel.class);
				logger.info("## res boardModel={}", body);
				logger.info("---------ok-----------");
			} else {
				logger.info("response error");

			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("fail to get osrm result !!!");
			return null;
		}

//    	try {
//    	    HttpPost httpPost = new HttpPost("www.example.com/api/board");
//    	    httpPost.setHeader("Accept", "application/json");
//    	    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//
//    	    List<NameValuePair> postParams = new ArrayList<NameValuePair>();
//    	    postParams.add(new BasicNameValuePair("param1", "data1"));
//    	    postParams.add(new BasicNameValuePair("param2", "data2"));
//
//    	    //Post 방식인 경우 데이터를 Request body message에 전송
//    	    HttpEntity postEntity = new UrlEncodedFormEntity(postParams, "UTF-8");
//    	    httpPost.setEntity(postEntity);
//
//    	    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//    	    CloseableHttpResponse response = httpClient.execute(httpPost);
//
//    	    if (response.getStatusLine().getStatusCode() == 200) {
//
//    	        ResponseHandler<String> handler = new BasicResponseHandler();
//    	        String body = handler.handleResponse(response);
//
//    	        //TypeReference<List<BoardModel>> typeReference = new TypeReference<List<BoardModel>>(){};
//    	        //List<BoardModel> boardModelList = objectMapper.readValue(body, typeReference);
//    	        //BoardModel boardModel = objectMapper.readValue(body, BoardModel.class);
//    	        //log.info("## res boardModel={}", boardModel);
//    	    }
//    	} catch(Exception e) {
//    	    e.printStackTrace();
//    	}

		return tripMatrix;
	}

	String getRoute(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution) {

		String routeResult = null;
		StringBuffer requestUrl = new StringBuffer("http://"+hostname+"/route/v1/car/");
		requestUrl.append(getDeliveryOrder(problem, solution));
		requestUrl.append("?geometries=geojson&overview=full");
		// http://192.168.6.45:5500/route/v1/car/127.1162273,37.5168241;127.1143369,37.5088933;127.1015711,37.5138507?geometries=geojson

		logger.info("url: " + requestUrl);

		try {
			HttpGet httpGet = new HttpGet(requestUrl.toString());
			httpGet.setHeader("Accept", "application/json");

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			CloseableHttpResponse response = httpClient.execute(httpGet);

			if (response.getStatusLine().getStatusCode() == 200) {

				Gson gson = new Gson();
				Type listType = new TypeToken<HashMap<String, Object>>() {
				}.getType();

				ResponseHandler<String> handler = new BasicResponseHandler();
				routeResult = handler.handleResponse(response);

				// TypeReference<List<BoardModel>> typeReference = new
				// TypeReference<List<BoardModel>>(){};
				// List<BoardModel> boardModelList = objectMapper.readValue(body,
				// typeReference);
				// BoardModel boardModel = objectMapper.readValue(body, BoardModel.class);
				logger.info("## res boardModel={}", routeResult);
				logger.info("---------ok-----------");
			} else {
				logger.info("response error");

			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("fail to get osrm result !!!");
			return null;
		}

		return routeResult;
	}

	private FastVehicleRoutingTransportCostsMatrix createMatrix() throws Exception {
		boolean loaded_job = load_jobfile(joblist, filepathRequestJob, true);

		if (loaded_job == false) {
			return null;
		}
		TripMatrixOSRM tripMatrix = requestRouteTable(joblist);
		if (tripMatrix == null) {
			return null;
		}

		FastVehicleRoutingTransportCostsMatrix.Builder builder = FastVehicleRoutingTransportCostsMatrix.Builder
				.newInstance(joblist.size(), false);

		int viaCount = tripMatrix.durations.size();
		for (int i = 0; i < viaCount; i++) { // start point index
			for (int j = 0; j < viaCount; j++) { // end point index
				builder.addTransportDistance(i, j, tripMatrix.distances.get(i)[j]);
				builder.addTransportTime(i, j, tripMatrix.durations.get(i)[j]);
			}
		}
		return builder.build();
	}

	boolean MakeResultShape(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution) {
		try {

			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

			String routeResult = getRoute(problem, solution);
			setRouteResult(problem, solution, routeResult);

			int tw_count = 0;
			for( job_item job : joblist) {
				if(job.tw_req > 0) {
					tw_count ++ ;
				}
			}

			String fileTag = String.format("[%s]_[TW%03d-%03d]", timeStamp,  tw_count, joblist.size());


			createRouteShapeFile("d:/test/route_"+fileTag+".shp", problem, solution, routeResult);
			createRoutePointShapeFile("d:/test/routept_"+fileTag+".shp", problem, solution, routeResult);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;

	}

	public String getDeliveryOrder(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution) {
		StringBuffer viaPoints = new StringBuffer();
		List<VehicleRoute> list = new ArrayList<VehicleRoute>(solution.getRoutes());
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

	public class waypoint {
		double distance;
		double[] location = new double[2];
		int start_vertex_idx = 0;
		int vertex_count = 0;
	}

	public class leg_item {
		double weight;
		double distance;
		double duration;
	}

	public int createRouteShapeFile(String shapefilepath, VehicleRoutingProblem problem,
			VehicleRoutingProblemSolution solution, String routeResult) throws Exception {
		String strShapefilePath = new String(shapefilepath);

		Gson gson = new Gson();

		// Map<String,Object> routeResult = getRoute(problem, solution);

		JsonArray obj_waypoints = JsonParser.parseString(routeResult).getAsJsonObject().getAsJsonArray("waypoints");

		Type typeWaypoints = new TypeToken<ArrayList<waypoint>>() {
		}.getType();
		ArrayList<waypoint> waypoints = (ArrayList<waypoint>) gson.fromJson(obj_waypoints, typeWaypoints);

		JsonArray obj_routes = null;

		if(hostname.contains(VER_524_PORTNO))
		{
			obj_routes = JsonParser.parseString(routeResult).getAsJsonObject().get("routes").getAsJsonArray();
		}
		else
		{
			obj_routes = JsonParser.parseString(routeResult).getAsJsonObject().get("route_summary").getAsJsonArray();
		}
		JsonArray obj_legs = obj_routes.get(0).getAsJsonObject().getAsJsonArray("legs");
		Type typeLeg = new TypeToken<ArrayList<leg_item>>() {
		}.getType();
		ArrayList<leg_item> legs = (ArrayList<leg_item>) gson.fromJson(obj_legs, typeLeg);

		JsonArray obj_geometry = obj_routes.get(0).getAsJsonObject().get("geometry").getAsJsonObject()
				.getAsJsonArray("coordinates");
		// JsonArray coodinates = obj_geometry.getAsJsonArray();
		Type typeCoord = new TypeToken<ArrayList<double[]>>() {
		}.getType();
		ArrayList<double[]> vertices = (ArrayList<double[]>) gson.fromJson(obj_geometry, typeCoord);

		int waypoint_idx = 0;
		int prev_waypoint_idx = 0;
		for (int vtx_loop = 0; vtx_loop < vertices.size(); vtx_loop++) {
			if (vertices.get(vtx_loop)[0] == waypoints.get(waypoint_idx).location[0]) {
				waypoints.get(waypoint_idx).start_vertex_idx = prev_waypoint_idx;
				waypoints.get(waypoint_idx).vertex_count = vtx_loop - prev_waypoint_idx;
				prev_waypoint_idx = vtx_loop;
				waypoint_idx++;
			}
		}

		SimpleFeatureType fmtShpDbf = createFeatureType("LINE");

		System.out.println("TYPE:" + fmtShpDbf);

		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(fmtShpDbf);

		File fShpFile = new File(strShapefilePath); // shp file obj 생성
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

		// 파일생성 parameter 설정
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", fShpFile.toURI().toURL());
		params.put("create spatial index", Boolean.FALSE);

		ShapefileDataStore shpfileDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
		shpfileDataStore.createSchema(fmtShpDbf);
		Transaction transaction = new DefaultTransaction("create");

		// m_transaction = new DefaultTransaction("create");
		String typeName = shpfileDataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource = shpfileDataStore.getFeatureSource(typeName);
		SimpleFeatureStore featureStore = null;

		if (featureSource instanceof SimpleFeatureStore) {
			featureStore = (SimpleFeatureStore) featureSource;
			featureStore.setTransaction(transaction);
		} else {
			System.out.println(typeName + " does not support read/write access");
			System.exit(1);
		}
		SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();

		/*
		 * The Shapefile format has a couple limitations: - "the_geom" is always first,
		 * and used for the geometry attribute name - "the_geom" must be of type Point,
		 * MultiPoint, MuiltiLineString, MultiPolygon - Attribute names are limited in
		 * length - Not all data types are supported (example Timestamp represented as
		 * Date)
		 *
		 * Each data store has different limitations so check the resulting
		 * SimpleFeatureType.
		 */
		System.out.println("SHAPE:" + SHAPE_TYPE);
		List<SimpleFeature> featuresRecord = new ArrayList<SimpleFeature>();
		List<VehicleRoute> list = new ArrayList<VehicleRoute>(solution.getRoutes());
		Collections.sort(list, new com.graphhopper.jsprit.core.util.VehicleIndexComparator());

		int route_order = 0;
		int vtx_index = 0;
		for (waypoint wp : waypoints) {
			if (wp.vertex_count < 1)
				continue;

			org.locationtech.jts.geom.Coordinate[] linecoord = new org.locationtech.jts.geom.Coordinate[wp.vertex_count
					+ 1];
			for (int vtx_loop1 = 0; vtx_loop1 <= wp.vertex_count; vtx_loop1++) {
				vtx_index = vtx_loop1 + wp.start_vertex_idx;

				linecoord[vtx_loop1] = new org.locationtech.jts.geom.Coordinate(vertices.get(vtx_index)[0],
						vertices.get(vtx_index)[1]);
			}

			LineString line_geom = geometryFactory.createLineString(linecoord);
			job_item job = routeResultList.get(route_order+1);//line이므로 line의 종점을 기준으로 속성을 입력한다.

			featureBuilder.add(line_geom);

			featureBuilder.add(route_order);
			featureBuilder.add(job.index);
			featureBuilder.add(job.tm_arrival);
			featureBuilder.add(job.tm_end);
			featureBuilder.add(job.tw_req_start);
			featureBuilder.add(job.tw_req_end);

			featureBuilder.add(job.sum_cost);
			featureBuilder.add(job.tbl_duration);
			featureBuilder.add(job.tbl_distance);
			featureBuilder.add(job.last_cost);
			featureBuilder.add(job.last_route_duration);
			featureBuilder.add(job.last_route_distance);

			SimpleFeature feature = featureBuilder.buildFeature(null);
			featuresRecord.add(feature);

			route_order++;

		}
		SimpleFeatureCollection collection = new ListFeatureCollection(fmtShpDbf, featuresRecord);
		featureStore.addFeatures(collection);
		transaction.commit();
		featuresRecord.clear(); // commit time에

		transaction.close();

		return 0;
	}



	public int createRoutePointShapeFile(String shapefilepath, VehicleRoutingProblem problem,
			VehicleRoutingProblemSolution solution, String routeResult) throws Exception {
		String strShapefilePath = new String(shapefilepath);

		Gson gson = new Gson();

		// Map<String,Object> routeResult = getRoute(problem, solution);
		// String routeResult = getRoute(problem, solution);

		JsonArray obj_waypoints = JsonParser.parseString(routeResult).getAsJsonObject().getAsJsonArray("waypoints");

		Type typeWaypoints = new TypeToken<ArrayList<waypoint>>() {
		}.getType();
		ArrayList<waypoint> waypoints = (ArrayList<waypoint>) gson.fromJson(obj_waypoints, typeWaypoints);

		SimpleFeatureType fmtShpDbf = createFeatureType("POINT");

		System.out.println("TYPE:" + fmtShpDbf);

		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(fmtShpDbf);

		File fShpFile = new File(strShapefilePath); // shp file obj 생성
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

		// 파일생성 parameter 설정
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", fShpFile.toURI().toURL());
		params.put("create spatial index", Boolean.FALSE);

		ShapefileDataStore shpfileDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
		shpfileDataStore.createSchema(fmtShpDbf);
		Transaction transaction = new DefaultTransaction("create");

		// m_transaction = new DefaultTransaction("create");
		String typeName = shpfileDataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource = shpfileDataStore.getFeatureSource(typeName);
		SimpleFeatureStore featureStore = null;

		if (featureSource instanceof SimpleFeatureStore) {
			featureStore = (SimpleFeatureStore) featureSource;
			featureStore.setTransaction(transaction);
		} else {
			System.out.println(typeName + " does not support read/write access");
			System.exit(1);
		}
		SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();

		/*
		 * The Shapefile format has a couple limitations: - "the_geom" is always first,
		 * and used for the geometry attribute name - "the_geom" must be of type Point,
		 * MultiPoint, MuiltiLineString, MultiPolygon - Attribute names are limited in
		 * length - Not all data types are supported (example Timestamp represented as
		 * Date)
		 *
		 * Each data store has different limitations so check the resulting
		 * SimpleFeatureType.
		 */
		System.out.println("SHAPE:" + SHAPE_TYPE);
		List<SimpleFeature> featuresRecord = new ArrayList<SimpleFeature>();
		List<VehicleRoute> list = new ArrayList<VehicleRoute>(solution.getRoutes());
		Collections.sort(list, new com.graphhopper.jsprit.core.util.VehicleIndexComparator());

		int route_order = 0;
		int vtx_index = 0;
		for (job_item job : routeResultList) {
			org.locationtech.jts.geom.Coordinate point_coord = new org.locationtech.jts.geom.Coordinate(job.mx,job.my);

			Point point_geom = geometryFactory.createPoint(point_coord);

			featureBuilder.add(point_geom);

			featureBuilder.add(route_order);
			featureBuilder.add(job.index);
			featureBuilder.add(job.tm_arrival);
			featureBuilder.add(job.tm_end);
			featureBuilder.add(job.tw_req_start);
			featureBuilder.add(job.tw_req_end);

			featureBuilder.add(job.sum_cost);
			featureBuilder.add(job.tbl_duration);
			featureBuilder.add(job.tbl_distance);
			featureBuilder.add(job.last_cost);
			featureBuilder.add(job.last_route_duration);
			featureBuilder.add(job.last_route_distance);


			SimpleFeature feature = featureBuilder.buildFeature(null);
			featuresRecord.add(feature);

			route_order++;

		}
		SimpleFeatureCollection collection = new ListFeatureCollection(fmtShpDbf, featuresRecord);
		featureStore.addFeatures(collection);
		transaction.commit();
		featuresRecord.clear(); // commit time에

		transaction.close();

		return 0;

	}

	private SimpleFeatureType createFeatureType(String geomType) {

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName(geomType);// LINE , POINT

		builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate
		// reference system
		/*
		 * CoordinateReferenceSystem targetCRS = null;
		 *
		 * try { targetCRS = CRS.decode("EPSG:5179"); } catch
		 * (NoSuchAuthorityCodeException e) { e.printStackTrace(); } catch
		 * (FactoryException e) { e.printStackTrace(); } builder.setCRS(targetCRS); //
		 * <- Coordinate reference system
		 */
		// add attributes in order
		if (geomType.equals("LINE"))
			builder.add("the_geom", LineString.class);
		else if (geomType.equals("POINT"))
			builder.add("the_geom", Point.class);
		else
			return null;
		builder.length(3).add("ORDER", Integer.class); //
		builder.length(3).add("ORIIDX", Integer.class); //
		builder.length(6).add("ARRTM", Integer.class); //
		builder.length(6).add("ENDTM", Integer.class); //
		builder.length(6).add("TWSTART", Integer.class); //
		builder.length(6).add("TWEND", Integer.class); //

		builder.length(6).add("COST", Integer.class); //
		builder.length(6).add("TBTRDUR", Integer.class); //
		builder.length(6).add("TBTRDIST", Integer.class); //
		builder.length(6).add("LSTCOST", Integer.class); //
		builder.length(6).add("RPTRDUR", Integer.class); //
		builder.length(6).add("RPTRDIST", Integer.class); //


		// build the type
		final SimpleFeatureType lineFeatureType = builder.buildFeatureType();

		return lineFeatureType;
	};


	public void setRouteResult(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution,
			String routeResult) {

		List<VehicleRoute> list = new ArrayList<VehicleRoute>(solution.getRoutes());
		Collections.sort(list, new com.graphhopper.jsprit.core.util.VehicleIndexComparator());
		int tw_count =0, tw_fail_count = 0;
		int visit_order = 0;
		job_item job_rst = null;
		for (VehicleRoute route : list) {
			double costs = 0;

			TourActivity prevAct = route.getStart();
			// start pos

			job_rst = new job_item();
			job_rst.index = route.getStart().getIndex();
			job_rst.order = visit_order; // Integer.parseInt(array[1]);
			job_rst.x = route.getStart().getLocation().getCoordinate().getX();
			job_rst.y = route.getStart().getLocation().getCoordinate().getY();

			job_rst.tm_end = (int) Math.round(route.getStart().getEndTime());
			job_rst.tm_arrival = (int) Math.round(route.getStart().getArrTime());
			job_rst.job_id = route.getStart().getName();
			job_rst.job_type = jobitem_type.job_start;


			visit_order++;
			routeResultList.add(job_rst);

			for (TourActivity act : route.getActivities()) {
				String jobId;
				if (act instanceof TourActivity.JobActivity) {
					jobId = ((TourActivity.JobActivity) act).getJob().getId();
				} else {
					jobId = "-";
				}
				double c = problem.getTransportCosts().getTransportCost(prevAct.getLocation(), act.getLocation(),
						prevAct.getEndTime(), route.getDriver(), route.getVehicle());
				c += problem.getActivityCosts().getActivityCost(act, act.getArrTime(), route.getDriver(),
						route.getVehicle());
				costs += c;

				job_rst = new job_item();

				job_rst.tw_req_start = act.getTheoreticalEarliestOperationStartTime() ;
				job_rst.tw_req_end = act.getTheoreticalLatestOperationStartTime() ;
				if(job_rst.tw_req_start == 0 &&job_rst.tw_req_end == Double.MAX_VALUE)
					job_rst.tw_req_end = 0;

				job_rst.tm_end = act.getEndTime();
				job_rst.tm_arrival = act.getArrTime();
				job_rst.job_id = jobId;

				job_rst.index = act.getIndex();
				job_rst.order = visit_order; // Integer.parseInt(array[1]);
				job_rst.x = act.getLocation().getCoordinate().getX();
				job_rst.y = act.getLocation().getCoordinate().getY();

				job_rst.tm_end = act.getEndTime();
				job_rst.tm_arrival = act.getArrTime();
				job_rst.sum_cost = costs;
				job_rst.last_cost = c;
				if(prevAct.getIndex() == -1)
				{
					job_rst.tbl_duration = tripMatrixforDebug.durations.get(prevAct.getLocation().getIndex())[act.getIndex()];
					job_rst.tbl_distance = tripMatrixforDebug.distances.get(prevAct.getLocation().getIndex())[act.getIndex()];
				}
				else {
					job_rst.tbl_duration = tripMatrixforDebug.durations.get(prevAct.getIndex())[act.getIndex()];
					job_rst.tbl_distance = tripMatrixforDebug.distances.get(prevAct.getIndex())[act.getIndex()];
				}
				//job_rst.tw_req_start = problem.

				job_rst.job_type = jobitem_type.job_delivery;

				routeResultList.add(job_rst);

				visit_order++;
				prevAct = act;
			}
			double c = problem.getTransportCosts().getTransportCost(prevAct.getLocation(), route.getEnd().getLocation(),
					prevAct.getEndTime(), route.getDriver(), route.getVehicle());
			c += problem.getActivityCosts().getActivityCost(route.getEnd(), route.getEnd().getArrTime(),
					route.getDriver(), route.getVehicle());
			costs += c;

			job_rst = new job_item();
			job_rst.index = route.getEnd().getIndex();
			job_rst.order = visit_order; // Integer.parseInt(array[1]);
			job_rst.x = route.getEnd().getLocation().getCoordinate().getX();
			job_rst.y = route.getEnd().getLocation().getCoordinate().getY();

			job_rst.tm_end = route.getEnd().getEndTime();
			job_rst.tm_arrival = route.getEnd().getArrTime();
			job_rst.job_id = route.getEnd().getName();

			job_rst.sum_cost = costs;
			job_rst.last_cost = c;
			job_rst.tbl_duration = tripMatrixforDebug.durations.get(prevAct.getIndex())[route.getEnd().getLocation().getIndex()];
			job_rst.tbl_distance = tripMatrixforDebug.distances.get(prevAct.getIndex())[route.getEnd().getLocation().getIndex()];
			//job_rst.last_distance = ;
			job_rst.job_type = jobitem_type.job_end;

			visit_order++;
			routeResultList.add(job_rst);

		}
		if (!solution.getUnassignedJobs().isEmpty()) {

			for (Job j : solution.getUnassignedJobs()) {
				job_rst = new job_item();
				job_rst.job_id = j.getId();
				job_rst.index = j.getIndex();
				job_rst.job_type = jobitem_type.job_unassigned;

				jobUnassignedList.add(job_rst);
			}
		}

		// -- route result parsing
		Gson gson = new Gson();
		JsonArray obj_waypoints = JsonParser.parseString(routeResult).getAsJsonObject().getAsJsonArray("waypoints");

		Type typeWaypoints = new TypeToken<ArrayList<waypoint>>() {
		}.getType();
		ArrayList<waypoint> waypoints = (ArrayList<waypoint>) gson.fromJson(obj_waypoints, typeWaypoints);

		JsonArray obj_routes = null;
		if(hostname.contains(VER_524_PORTNO))
		{
			obj_routes = JsonParser.parseString(routeResult).getAsJsonObject().get("routes").getAsJsonArray();
		}
		else
		{
			obj_routes = JsonParser.parseString(routeResult).getAsJsonObject().get("route_summary").getAsJsonArray();
		}

		JsonArray obj_legs = obj_routes.get(0).getAsJsonObject().getAsJsonArray("legs");
		Type typeLeg = new TypeToken<ArrayList<leg_item>>() {
		}.getType();
		ArrayList<leg_item> legs = (ArrayList<leg_item>) gson.fromJson(obj_legs, typeLeg);

		int waypoint_idx = 0;
		if(waypoints.size() != routeResultList.size())
		{
			logger.info("error waypoint count != route result size");
			return ;
		}

		for (waypoint via : waypoints) {
			job_item job = routeResultList.get(waypoint_idx);
			job.mx = via.location[0];
			job.my = via.location[1];
			waypoint_idx++;
		}

		if(legs.size() != routeResultList.size()-1)
		{
			logger.info("error legs count != route result size -1 ");
			return ;
		}

		int leg_index = 1;
		for (leg_item leg : legs) {
			job_item job = routeResultList.get(leg_index);
			job.last_route_distance = leg.distance;
			job.last_route_duration = leg.duration;
			job.last_route_weight = leg.weight;
			leg_index++;
		}

		job_item job_req = null;

		for( job_item rst : routeResultList ) {
			if( rst.index >= 0 ) {
				job_req = joblist.get(rst.index);
				rst.poi_name = job_req.poi_name;
				if(job_req.tw_req > 0 ) {
					rst.tw_req = job_req.tw_req;

//					rst.tw_req_start = job_req.tw_req_start;
//					rst.tw_req_end = job_req.tw_req_end;
				}
			}
		}

		StringBuffer jobDesc = new StringBuffer();
		jobDesc.append("JOB_ID").append("\t");
		jobDesc.append("INDEX").append("\t");
		jobDesc.append("ORDER").append("\t");
		jobDesc.append("POI").append("\t");
		jobDesc.append("X").append("\t");
		jobDesc.append("Y").append("\t");
		jobDesc.append("MM-X").append("\t");
		jobDesc.append("MM-Y").append("\t");
		jobDesc.append("LASTCOST").append("\t");
		jobDesc.append("TBL_DURA").append("\t");
		jobDesc.append("TBL_DIST").append("\t");
		jobDesc.append("ARR_TM").append("\t");
		jobDesc.append("END_TM").append("\t");
		jobDesc.append("TW_OK").append("\t");
		jobDesc.append("TW_START").append("\t");
		jobDesc.append("TW_END").append("\t");
		jobDesc.append("LAST_RP_DURA").append("\t");
		jobDesc.append("LAST_RP_DIST").append("\t");
		jobDesc.append("LAST_RP_WEIT").append("\t");
		System.out.println(jobDesc.toString());
		jobDesc.setLength(0);

		for( job_item job : routeResultList ) {
			jobDesc.append(job.job_id).append("\t");

			jobDesc.append(job.index).append("\t");
			jobDesc.append(job.order).append("\t");
			jobDesc.append(job.poi_name).append("\t");
			jobDesc.append(String.format("%.7f", job.x)).append("\t");
			jobDesc.append(String.format("%.7f", job.y)).append("\t");

			jobDesc.append(String.format("%.7f", job.mx)).append("\t");
			jobDesc.append(String.format("%.7f", job.my)).append("\t");

			jobDesc.append(String.format("%.1f", job.last_cost)).append("\t");
			jobDesc.append(String.format("%.1f", job.tbl_duration)).append("\t");
			jobDesc.append(String.format("%.1f", job.tbl_distance)).append("\t");

			jobDesc.append(String.format("%.1f", job.tm_arrival)).append("\t");
			jobDesc.append(String.format("%.1f", job.tm_end)).append("\t");

			if( job.tw_req > 0 )
			{
				tw_count ++ ;
				if(job.tm_arrival >= job.tw_req_start-0.1 && job.tm_arrival <= job.tw_req_end+0.1) {
					jobDesc.append("OK").append("\t");
				} else {
					jobDesc.append("FAIL").append("\t");
					tw_fail_count ++ ;
				}
			}else {
				jobDesc.append("-").append("\t");
			}


			jobDesc.append(String.format("%.1f", job.tw_req_start)).append("\t");
			jobDesc.append(String.format("%.1f", job.tw_req_end)).append("\t");

			jobDesc.append(String.format("%.1f", job.last_route_duration)).append("\t");
			jobDesc.append(String.format("%.1f", job.last_route_distance)).append("\t");
			jobDesc.append(String.format("%.1f", job.last_route_weight)).append("\t");

			System.out.println(jobDesc.toString());
			jobDesc.setLength(0);

		}

		// ------------------
		logger.debug("tw count = {}, fail={}, unassigned={}", tw_count, tw_fail_count, jobUnassignedList.size() );
	}

	// sHourMin은 반드시 HH:MM으로 되어야 한다.
	double convHMtoSec(String sHourMin) {
		String array[] = sHourMin.split(":");
		Double secHour = Double.parseDouble(array[0]) * 3600 ;
		Double secMin = Double.parseDouble(array[1]) * 60 ;
		return secHour+secMin;
	}

	//sec은 0 <= x < 86400 범위에 있어야 한다.
	String convSectoHM(double sec) {
		String HM = String.format("%02d:%02d", (int)(sec/3600), (int)((sec%3600)/60));
		return HM;
	}
}
