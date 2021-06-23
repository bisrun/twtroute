package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TwtResponse_Solution {
    ArrayList<TwtResponse_SolutionRoute> routes ;
    ArrayList<TwtResponse_SolutionUnassigned> unassigned ;
}
