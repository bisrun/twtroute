package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwtRequest_Vehicle {
    @JsonProperty("driver_id")
    private String driver_id;

    @JsonProperty("vehicle_id")
    private String vehicle_id;
    @JsonProperty("vehicle_type")
    private String vehicle_type;

    @JsonProperty("start_task_id")
    private String start_task_id;

    @JsonProperty("end_task_id")
    private String end_task_id;

    @JsonProperty("start_time")
    private double start_time;

    @JsonProperty("end_time")
    private double end_time;
}
