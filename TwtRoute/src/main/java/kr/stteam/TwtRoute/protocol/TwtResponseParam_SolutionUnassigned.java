package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TwtResponseParam_SolutionUnassigned {
    String task_id;
    int     req_task_index;
    private List<String> timewindow;
}
