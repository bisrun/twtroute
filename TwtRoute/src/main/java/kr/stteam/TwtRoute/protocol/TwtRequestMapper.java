package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TwtRequestMapper {
    private TwtRequestMapper() {
    }

    static public TwtRequest_Tsptw parseParam(String jsonParam){

        ObjectMapper mapper = new ObjectMapper();
        TwtRequest_Tsptw req = null;
        try {
            req = mapper.readValue(jsonParam, TwtRequest_Tsptw.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return req;
    }
}
