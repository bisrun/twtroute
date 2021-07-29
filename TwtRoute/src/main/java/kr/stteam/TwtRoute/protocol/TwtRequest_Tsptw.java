package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

public class TwtRequest_Tsptw {
    @JsonProperty("protocol_ver")
    private String protocol_ver;

    @JsonProperty("options")
    private TwtRequest_Options options;

    @JsonProperty("vehicle")
    private TwtRequest_Vehicle vehicle;

    @JsonProperty("services")
    private List<TwtRequest_Service> services;

    @JsonProperty("jobId")
    private String jobId;

    @JsonIgnore
    public TwtRequest_Service getStartTask(){
        String findId= vehicle.getStart_task_id();

        for( TwtRequest_Service item : services) {
            if (item.getTask_id().compareToIgnoreCase(findId) == 0)
                return item;
        }
        return null;
    };
    @JsonIgnore
    public int getStartTaskIdx(){
        String findId= vehicle.getStart_task_id();
        int index = 0;
        for( TwtRequest_Service item : services) {
            if (item.getTask_id().compareToIgnoreCase(findId) == 0)
                return index;
            index ++;
        }
        return -1;
    };

    @JsonIgnore
    public TwtRequest_Service getEndTask( ){
        String findId= vehicle.getEnd_task_id();
        if( findId == null ) return null ;
        for( TwtRequest_Service item : services) {
            if( item.getTask_id().compareToIgnoreCase(findId) == 0 )
                return item;
        }
        return null;
    };
    @JsonIgnore
    public int getEndTaskIdx(){
        String findId= vehicle.getEnd_task_id();
        if( findId == null ) return -1 ;

        int index = 0;
        for( TwtRequest_Service item : services) {
            if (item.getTask_id().compareToIgnoreCase(findId) == 0)
                return index;
            index ++;
        }
        return -1;
    };
}
