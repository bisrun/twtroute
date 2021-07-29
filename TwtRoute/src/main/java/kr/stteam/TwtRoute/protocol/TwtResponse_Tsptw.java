package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
//@EqualsAndHashCode(callSuper=false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TwtResponse_Tsptw extends TwtResponse_Base{
    //String job_id;
    //String status; //http status..
    double processing_time;

    TwtResponse_Solution solution;

    public TwtResponse_Tsptw() {
    }

    public TwtResponse_Tsptw(TwtResponse_forAssignJob result) {
        this.job_id = result.getJob_id();
        this.status = result.getStatus();
    }
//    public TwtResponse_Base(TwtResponse_Base rs) {
//        this.job_id = rs.getJob_id();
//        this.status = rs.getStatus();
//        this.processing_time = rs.getProcessing_time();
//        this.solution = rs.getSolution();
//    }
}
