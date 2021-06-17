package kr.stteam.TwtRoute.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.stteam.TwtRoute.AppProperties;
import kr.stteam.TwtRoute.controller.TwtResult;
import kr.stteam.TwtRoute.protocol.*;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import scala.App;

import java.util.ArrayList;

@Service
public class RouteProcOSRM implements RouteProc{
    private static Logger logger = LoggerFactory.getLogger(RouteProcOSRM.class);
    private AppProperties appProperties;
    final String OSRM_VER_524_PORTNO = new String("20000");

    @Autowired
    public  RouteProcOSRM(AppProperties appProperties){
        this.appProperties = appProperties;
    }

    @Override
    public String requestTripMatrix(StringBuffer viaInfo) {

        StringBuffer requestUrl = new StringBuffer("http://" + appProperties.getOsrmServerIpPort() + "/table/v1/car/");
        requestUrl.append(viaInfo);
        if (appProperties.getOsrmServerPort().contains(OSRM_VER_524_PORTNO)){ // osrm 5.24
            requestUrl.append("?annotations=distance,duration");
        }
        logger.info("url: " + requestUrl);

        try {
            HttpGet httpGet = new HttpGet(requestUrl.toString());
            httpGet.setHeader("Accept", "application/json");

            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            CloseableHttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {

                ResponseHandler<String> handler = new BasicResponseHandler();
                return handler.handleResponse(response); // success

            } else {
                logger.error("fail to get osrm maxtrix result - not 200");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("fail to get osrm maxtrix result - exception");
            return null;
        }
        return null;
    }

    @Override
    public boolean setTripMatrixInResult(String responseJson, TwtResult twtResult) {
        ObjectMapper mapper = new ObjectMapper();
        OsrmTripMatrixResponseParam tripMatrix = null;
        try {
            tripMatrix = mapper.readValue(responseJson, OsrmTripMatrixResponseParam.class);

            if (tripMatrix.getDistances().size() == 0) {
                for (double[] ar : tripMatrix.getDurations()) {
                    double[] arDist = ar.clone();
                    tripMatrix.getDistances().add(arDist); // distance가 없어서,
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
        tripMatrix.setTaskCount(tripMatrix.getDurations().size());
        twtResult.setTripMatrix(tripMatrix);

        return true;
    }

    @Override
    public String requestRouteGeometry(TwtResult twtResult) {
        String body = null;

        StringBuffer requestUrl = new StringBuffer("http://" + appProperties.getOsrmServerIpPort()  + "/route/v1/car/");
        requestUrl.append(twtResult.GetOrderedWaypoint());
        requestUrl.append("?geometries=geojson&overview=full");
        // http://192.168.6.45:5500/route/v1/car/127.1162273,37.5168241;127.1143369,37.5088933;127.1015711,37.5138507?geometries=geojson

        logger.info("url: " + requestUrl);

        try {
            HttpGet httpGet = new HttpGet(requestUrl.toString());
            httpGet.setHeader("Accept", "application/json");

            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            CloseableHttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {

                ResponseHandler<String> handler = new BasicResponseHandler();
                body = handler.handleResponse(response);


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

    @Override
    public boolean setRouteGeometryInResult(String responseJson,TwtResult twtResult) {
        int waypoint_idx = 0;
        int prev_waypoint_idx = 0;
        ObjectMapper mapper = new ObjectMapper();
        try {
            twtResult.osrmRouteResponse = mapper.readValue(responseJson, OsrmRouteResponseParam_Base.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }

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
}
