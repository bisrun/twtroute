package kr.stteam.TwtRoute.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.stteam.TwtRoute.protocol.TwtRequestParam_BaseData;

public class TwtRequestParameter {
    private TwtRequestParameter() {
    }

    static public TwtRequestParam_BaseData parseParam(String jsonParam){

        ObjectMapper mapper = new ObjectMapper();
        TwtRequestParam_BaseData req = null;
        try {
            req = mapper.readValue(jsonParam, TwtRequestParam_BaseData.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return req;
    }
}
