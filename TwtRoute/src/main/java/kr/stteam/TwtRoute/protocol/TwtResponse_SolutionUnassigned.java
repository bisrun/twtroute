package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TwtResponse_SolutionUnassigned {
    String  task_id;
    int     req_task_index;
    double[] time_window;

}
