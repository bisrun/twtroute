package kr.stteam.TwtRoute.protocol;

import lombok.Data;

@Data
public class TwtResponse_Base {
    String job_id;
    String status;
    double processing_time;
    TwtResponse_Solution solution;
}
