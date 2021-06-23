package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwtRequest_Service {
    @JsonProperty("task_id")
    private String task_id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("loc_coord")
    private List<Double> loc_coord ;

    @JsonProperty("dir_angle")
    private int    dir_angle ;

    @JsonProperty("keep_entry_angle")
    private boolean keep_entry_angle;

    @JsonProperty("time_window")
    private List<Double> time_window;

    @JsonProperty("service_time")
    private int service_time;
}
