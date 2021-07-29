package kr.stteam.TwtRoute.protocol;

import lombok.Data;

@Data
public class TwtResponse_Base {
    String job_id;
    StatusType status; //http status..
    String status_time; // 상태등록시간

    public enum StatusType {
        Ok(0),
        Ok_including_unassinged(10),
        Fail(100);

        private int value;
        StatusType(int value) {
            this.value = value;
        }
    };

}
