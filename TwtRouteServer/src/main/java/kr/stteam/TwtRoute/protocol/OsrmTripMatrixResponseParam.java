package kr.stteam.TwtRoute.protocol;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.ArrayList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

public class OsrmTripMatrixResponseParam {
    @JsonProperty("code")
    String code;

    @JsonProperty("durations")
    private ArrayList<double[]> durations = new ArrayList<double[]>();

    @JsonProperty("distances")
    private ArrayList<double[]> distances = new ArrayList<double[]>();

    @JsonIgnore
    private int taskCount;
//    @JsonIgnore
//    private ArrayList<String> sources = new  ArrayList<String>();
//
//    @JsonIgnore
//    private ArrayList<String> destinations = new  ArrayList<String>();
}
