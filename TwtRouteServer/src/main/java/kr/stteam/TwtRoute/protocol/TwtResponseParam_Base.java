package kr.stteam.TwtRoute.protocol;

import lombok.Data;

@Data
public class TwtResponseParam_Base {
    String job_id;
    String status;
    //String waiting_time_in_queue;
    int tms_processing;
    TwtResponseParam_Solution solution;
}
