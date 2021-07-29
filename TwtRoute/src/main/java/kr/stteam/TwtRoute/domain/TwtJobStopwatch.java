package kr.stteam.TwtRoute.domain;

public class TwtJobStopwatch {
    //

    // s1_1: receive first byte of request
    // s1_2: receive all request
    // s1_3: parsing request json
    // s2_1: start to get matrix table from osrm
    // s2_2: end to matrix table
    // s3_1: start to calculate tsptw
    // ....
    // s3_2: end to calculate tsptw
    // s4_1:start to collect solution
    // s4_2_1: start to get route geom from osrm
    // s4_2_2: end to get route geom
    // s4_3: end to collect solution
    // s5_1:packaging json - x ,
    // s5_2:send response json to client - x

    public double   proc_start_time = 0;
    public double   proc_total_time_before_resp_json =0;
    public double   proc_total_time_after_resp_json =0;
    public double   proc_end_time_before_resp_json = 0;
    public double   proc_end_time_after_resp_json = 0;
    public double   proc_getmatrix_time =0;
}
