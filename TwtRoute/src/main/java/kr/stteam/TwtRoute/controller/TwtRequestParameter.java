package kr.stteam.TwtRoute.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.stteam.TwtRoute.protocol.TwtRequest_Base;

public class TwtRequestParameter {
    private TwtRequestParameter() {
    }

    static public TwtRequest_Base parseParam(String jsonParam){

        ObjectMapper mapper = new ObjectMapper();
        TwtRequest_Base req = null;
        try {
            req = mapper.readValue(jsonParam, TwtRequest_Base.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return req;
    }
}
