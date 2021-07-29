package kr.stteam.TwtRoute.protocol;

import lombok.Data;

@Data
public class TwtResponseWrapper {
    TwtResponse_Tsptw response;
    String processStatus; //현재 처리 상태를 나타냄

    public TwtResponseWrapper(TwtResponse_Tsptw response, String processStatus) {
        this.response = response;
        this.processStatus = processStatus;
    }
    public TwtResponseWrapper(TwtResponse_forAssignJob response, String processStatus) {
        this.response = new TwtResponse_Tsptw(response);
        this.processStatus = processStatus;
    }
}
