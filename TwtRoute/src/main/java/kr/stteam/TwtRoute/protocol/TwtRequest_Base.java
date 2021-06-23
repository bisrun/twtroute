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

public class TwtRequest_Base {
    @JsonProperty("protocol_ver")
    private String protocol_ver;

    @JsonProperty("options")
    private TwtRequest_Options options;

    @JsonProperty("vehicle")
    private TwtRequest_Vehicle vehicle;

    @JsonProperty("services")
    private List<TwtRequest_Service> services;

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
    public TwtRequest_Service getEndTask( ){
        String findId= vehicle.getEnd_task_id();

        for( TwtRequest_Service item : services) {
            if( item.getTask_id().compareToIgnoreCase(findId) == 0 )
                return item;
        }
        return null;
    };
}
