package kr.stteam.TwtRoute.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TwtResponse_Error extends TwtResponse_Base{
    String message;

    public TwtResponse_Error(TwtResponse_Base.StatusType status, String message) {
        this.status = status;
        this.message = message;
    }
}
