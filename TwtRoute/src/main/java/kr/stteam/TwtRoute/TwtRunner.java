package kr.stteam.TwtRoute;

import kr.stteam.TwtRoute.service.TwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TwtRunner implements ApplicationRunner {
    private Logger logger = LoggerFactory.getLogger(TwtRunner.class);
    private AppProperties appProperties;

    @Autowired
    public TwtRunner(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        //logger.info("osrmserver ip port : "+ appProperties.getOsrmServerIpPort());
    }
}
