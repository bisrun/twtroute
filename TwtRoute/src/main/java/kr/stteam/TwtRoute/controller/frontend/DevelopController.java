package kr.stteam.TwtRoute.controller.frontend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.stteam.TwtRoute.protocol.*;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

class ESRIShapeFileFactory {
    static class Pair {
        public String first;
        public Class<?> second;

        public Pair(String first, Class<?> second) {
            this.first = first;
            this.second = second;
        }
    }
    final private static List<Pair> DBFS = new ArrayList<>(Arrays.asList(
        new Pair("TASK_ID", String.class),
        new Pair("TASK_TYPE", String.class),
        new Pair("LOC_NAME", String.class),
        new Pair("DURATION", Double.class),
        new Pair("DISTANCE", Double.class),
        new Pair("REQ_INDEX", Integer.class),    // field name length 가 10을 넘어가면 value 가 안 적힘
        new Pair("TASK_ORDER", Integer.class),
        new Pair("ARRIVE_TM", String.class),     // field name length 가 10을 넘어가면 value 가 안 적힘
        new Pair("END_TIME", String.class)
    ));

    private static String secToHHMMSS(final double t) {
        final int hh = (int)t / 3600;
        final int mm = ((int)t - (3600 * hh)) / 60;
        final int ss = ((int)t- (3600*hh)) - (60 *mm);
        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }

    private static void setDbfToFeatureBuilder(final TwtResponse_RouteActivity routeActivity, SimpleFeatureBuilder featureBuilder) {
        featureBuilder.add(routeActivity.getTask_id());
        featureBuilder.add(routeActivity.getTask_type());
        featureBuilder.add(routeActivity.getLoc_name());
        featureBuilder.add(routeActivity.getDuration());
        featureBuilder.add(routeActivity.getDistance());
        featureBuilder.add(routeActivity.getReq_task_index());
        featureBuilder.add(routeActivity.getTask_order());
        featureBuilder.add(secToHHMMSS(routeActivity.getArrival_time()));
        featureBuilder.add(secToHHMMSS(routeActivity.getEnd_time()));
    }

    private static SimpleFeatureTypeBuilder getSimpleFeatureTypeBuilder(final Class<?> className, final List<Pair> dbfs) {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

        b.setName("author");
        b.setCRS(DefaultGeographicCRS.WGS84); // Coordinate Reference System
        // the_geom 은 고정
        b.add("the_geom", className);
        dbfs.forEach(dbf -> {
            b.add(dbf.first, dbf.second);
        });
        return b;
    }

    private static void flushShape(final SimpleFeatureType TYPE, final List<SimpleFeature> features,
                                   final String dir, final String name) throws Exception {
        File shpFile = new File(dir, name);
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", shpFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = null;
        newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        newDataStore.createSchema(TYPE);

        // Write the feature data to the shape file
        Transaction transaction = new DefaultTransaction("create");
        String typeName = null;
        SimpleFeatureSource featureSource = null;
        typeName = newDataStore.getTypeNames()[0];
        featureSource = newDataStore.getFeatureSource(typeName);
        SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();
        System.out.println("SHAPE:" + SHAPE_TYPE);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (Exception problem) {
                transaction.rollback();
            } finally {
                transaction.close();
            }
        }
    }

    // point shape
    public static boolean createPointShapeFile(final String dir, final TwtResponse_Tsptw response) throws Exception {
        SimpleFeatureTypeBuilder b = getSimpleFeatureTypeBuilder(Point.class, DBFS);
        final SimpleFeatureType TYPE = b.buildFeatureType();

        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

        org.locationtech.jts.geom.GeometryFactory geometryFactory =
            JTSFactoryFinder.getGeometryFactory();

        TwtResponse_SolutionRoute route = response.getSolution().getRoutes().get(0);
        route.getActivities().forEach(routeActivity -> {
            final double[] coords = routeActivity.getLoc_coord();
            final Point point = geometryFactory.createPoint(new Coordinate(coords[0], coords[1]));
            featureBuilder.add(point);
            // dbf
            setDbfToFeatureBuilder(routeActivity, featureBuilder);
            // set feature
            SimpleFeature feature = featureBuilder.buildFeature(null);
            features.add(feature);
        });

        flushShape(TYPE, features, dir, response.getJob_id() + "_point.shp");
        return true;
    }

    public static boolean createLineShapeFile(final String dir, final TwtResponse_Tsptw response) throws Exception {
        SimpleFeatureTypeBuilder b = getSimpleFeatureTypeBuilder(LineString.class, DBFS);
        final SimpleFeatureType TYPE = b.buildFeatureType();

        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

        org.locationtech.jts.geom.GeometryFactory geometryFactory =
            JTSFactoryFinder.getGeometryFactory();

        TwtResponse_SolutionRoute route = response.getSolution().getRoutes().get(0);
        route.getActivities().forEach(routeActivity -> {
            if (routeActivity.getGeometry() == null) {
                return;
            }

            List<Coordinate> coordinateList = new ArrayList<>();
            routeActivity.getGeometry().getCoordinates().forEach(coordinate -> {
                final Double x = coordinate[0];
                final Double y = coordinate[1];

                coordinateList.add(new Coordinate(x, y));
            });
            final Coordinate[] coords = coordinateList.toArray(new Coordinate[coordinateList.size()]);
            final LineString lineString = geometryFactory.createLineString(coords);
            featureBuilder.add(lineString);
            // dbf
            setDbfToFeatureBuilder(routeActivity, featureBuilder);
            // set feature
            SimpleFeature feature = featureBuilder.buildFeature(null);
            features.add(feature);
        });

        flushShape(TYPE, features, dir, response.getJob_id() + "_line.shp");
        return true;
    }
}

class ResultOfAny {
    private String result;
    private String urlPath;
    private String downloadName;

    public ResultOfAny(String result, String urlPath, String downloadName) {
        this.result = result;
        this.urlPath = urlPath;
        this.downloadName = downloadName;
    }

    public String getResult() {
        return this.result;
    }

    public String getUrlPath() {
        return this.urlPath;
    }

    public String getDownloadName() {
        return this.downloadName;
    }
}

@Controller
public class DevelopController {
    @Value("${external.resource.path}")
    private String externalResourcePath;

    private TwtRequest_Tsptw getTwtRequestObject() {
        TwtRequest_Tsptw requestTSPTW = new TwtRequest_Tsptw();
        TwtRequest_Options options = new TwtRequest_Options();
        TwtRequest_Vehicle vehicle = new TwtRequest_Vehicle();
        //
        options.setService_time(60);
        options.setReq_route_geom(true);
        options.setReq_debuginfo(false);
        options.setKeep_entry_angle(false);
        //
        vehicle.setDriver_id("hong");
        vehicle.setVehicle_id("car-0101");
        vehicle.setStart_task_id("0");
        vehicle.setEnd_task_id("0");
        vehicle.setStart_time(32400);
        vehicle.setEnd_time(64800);
        //
        requestTSPTW.setProtocol_ver("0.1.7");
        requestTSPTW.setOptions(options);
        requestTSPTW.setVehicle(vehicle);

        return requestTSPTW;
    }

    private List<ResultOfAny> getDownloadURLS(final File dir) {
        File[] list = dir.listFiles();
        List<ResultOfAny> results = new ArrayList<>();
        Arrays.stream(list).forEach(file -> {
            final String parent = new File(file.getParent()).getName();
            final String name = file.getName();
            final String urlPath = String.format("/%s/%s/%s", "file", parent, name);
            results.add(new ResultOfAny("success", urlPath, name));
        });
        return results;
    }

    @GetMapping("/develop")
    public String developerHome(@RequestParam("auth") String auth) {
        System.out.println(auth);
        return "frontend";
    }

    // Text => JSON
    @ResponseBody
    @PostMapping("/develop/convert/{fileName}")
    public ResultOfAny convertText(@RequestBody String fullText, @PathVariable("fileName") String fileName)  {
        TwtRequest_Tsptw twtRequest = getTwtRequestObject();
        List<TwtRequest_Service> services = new ArrayList<>();

        String[] splited = fullText.split("\n");
        Arrays.stream(splited, 1, splited.length).forEach(data -> {
            String[] elements = data.split("\t");
            final String ID = elements[0];
            final String poi =  elements[1];
            final Double y = Double.parseDouble(elements[2]);
            final Double x = Double.parseDouble(elements[3]);

            TwtRequest_Service service = new TwtRequest_Service();
            service.setTask_id(ID);
            service.setName(poi);
            service.setLoc_coord(new ArrayList<Double>(Arrays.asList(x, y)));

            if (elements.length >= 5 && elements[4].equals('1')) {
                final Double twStart = Double.parseDouble(elements[5]);
                final Double twEnd = Double.parseDouble(elements[6]);

                service.setTime_window(new ArrayList<Double>(Arrays.asList(twStart, twEnd)));
            }
            services.add(service);
        });
        twtRequest.setServices(services);

        ObjectMapper mapper = new ObjectMapper();
        try {
            final Integer last = fileName.lastIndexOf('.');
            final String tmp  = fileName.subSequence(0, last).toString() + ".json";

            File fileInDirectory = new File(externalResourcePath, tmp);
            mapper.writeValue(fileInDirectory, twtRequest);

            return new ResultOfAny("success", "/file/"+tmp, tmp);
        } catch (JsonProcessingException e) {
            //e.printStackTrace();
            return new ResultOfAny("fail", e.getMessage(), "");
        } catch (IOException e) {
            //e.printStackTrace();
            return new ResultOfAny("fail", e.getMessage(), "");
        }
    }

    // shape 관련 처리
    @ResponseBody
    @PostMapping("/develop/shape")
    public List<ResultOfAny> downloadShape(@RequestBody String fullData) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            TwtResponse_Tsptw response =  mapper.readValue(fullData, TwtResponse_Tsptw.class);
            final File tmp = new File(externalResourcePath, response.getJob_id());
            if (tmp.mkdir()) {
                ESRIShapeFileFactory.createPointShapeFile(tmp.getAbsolutePath(), response);
                ESRIShapeFileFactory.createLineShapeFile(tmp.getAbsolutePath(), response);
                return getDownloadURLS(tmp);
            } else {
                return new ArrayList<ResultOfAny>(Arrays.asList(
                    new ResultOfAny("fail", "The shape of job is already exist", "")
                ));
            }
        } catch (JsonProcessingException e) {
            //e.printStackTrace();
            return new ArrayList<ResultOfAny>(Arrays.asList(
                new ResultOfAny("fail", e.getMessage(), "")
            ));
        } catch (Exception e) {
            //e.printStackTrace();
            return new ArrayList<ResultOfAny>(Arrays.asList(
                new ResultOfAny("fail", e.getMessage(), "")
            ));
        }
    }
}
