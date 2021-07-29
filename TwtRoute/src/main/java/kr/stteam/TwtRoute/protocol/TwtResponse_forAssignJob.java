package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TwtResponse_forAssignJob extends TwtResponse_Base{
    //String job_id;
    //String status; //http status..
    String reg_job_time;
}
