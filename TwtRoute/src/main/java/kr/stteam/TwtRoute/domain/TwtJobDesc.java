package kr.stteam.TwtRoute.domain;

import java.util.ArrayList;

public class TwtJobDesc {
    private long id; //system auto increment id
    String  uuid;
    double  tmsStartTime;
    double  tmsEndtime;
    double  cost;
    double  duration;
    double  distance;

    boolean isRoundtrip;

    ArrayList<TwtTaskItem> taskList ;
    TwtVehicleDesc vehicleInfo;
}
