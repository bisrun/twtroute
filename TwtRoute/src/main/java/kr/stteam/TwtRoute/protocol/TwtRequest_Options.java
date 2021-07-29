package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;



@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwtRequest_Options {
    @JsonProperty("service_time")
    private double service_time;

    @JsonProperty("req_route_geom")
    private boolean req_route_geom;

    @JsonProperty("req_debuginfo")
    private boolean req_debuginfo;

    @JsonProperty("keep_entry_angle")
    private boolean keep_entry_angle;

    @JsonProperty(value="admin_thread_cnt",defaultValue = "-1")
    private Integer admin_thread_cnt;

    @JsonProperty(value="admin_max_iteration",defaultValue = "-1")
    private Integer admin_max_iteration;

    @JsonProperty(value="admin_algo_fast_regret",defaultValue = "false")
    private Boolean admin_algo_fast_regret;

    @JsonProperty(value="admin_algo_best_insertion",defaultValue = "false")
    private Boolean admin_algo_best_insertion;

    // osrm 서버를 이용한 1. matrix table, 2. route json을 파일로 저장한다.
    @JsonProperty(value="admin_save_route_result_json",defaultValue = "false")
    private Boolean admin_log_route_result_json;


}
