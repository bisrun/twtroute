package kr.stteam.TwtRoute;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties("twt")
@Validated
public class AppProperties {
    private int maxProcessTime;
    private String serverIpOSRM;
    private String serverPortOSRM;

    public int getMaxProcessTime() {
        return maxProcessTime;
    }

    public void setMaxProcessTime(int maxProcessTime) {
        this.maxProcessTime = maxProcessTime;
    }

    public String getServerIpOSRM() {
        return serverIpOSRM;
    }

    public void setServerIpOSRM(String serverIpOSRM) {
        this.serverIpOSRM = serverIpOSRM;
    }

    public String getServerPortOSRM() {
        return serverPortOSRM;
    }

    public void setServerPortOSRM(String serverPortOSRM) {
        this.serverPortOSRM = serverPortOSRM;
    }
}
