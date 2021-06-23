package kr.stteam.TwtRoute.protocol;

import lombok.Data;

@Data
public class DebugRoute {
    String arrival_time_hm;
    String end_time_hm;

    double      weight;
    double      transport_time;
    double      service_duration;
    double      waiting_time;
    double      total_duration;
}
