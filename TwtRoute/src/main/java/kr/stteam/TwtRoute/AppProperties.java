package kr.stteam.TwtRoute;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties("twt")
@Validated

public class AppProperties {
    private int maxProcessTime;
    private String osrmServerIp;
    private String osrmServerPort;
    private String osrmServerIpPort;

    public int getMaxProcessTime() {
        return maxProcessTime;
    }

    public void setMaxProcessTime(int maxProcessTime) {
        this.maxProcessTime = maxProcessTime;
    }

    public String getOsrmServerIp() {
        return osrmServerIp;
    }

    public void setOsrmServerIp(String osrmServerIp) {
        this.osrmServerIp = osrmServerIp;
    }

    public String getOsrmServerPort() {
        return osrmServerPort;
    }

    public void setOsrmServerPort(String osrmServerPort) {
        this.osrmServerPort = osrmServerPort;
    }

    public String getOsrmServerIpPort() {
        return osrmServerIpPort;
    }

    public void setOsrmServerIpPort(String osrmServerIpPort) {
        this.osrmServerIpPort = osrmServerIpPort;
    }
}
