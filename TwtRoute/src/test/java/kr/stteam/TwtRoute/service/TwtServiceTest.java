package kr.stteam.TwtRoute.service;

import kr.stteam.TwtRoute.AppProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Disabled
class TwtServiceTest {
    @Autowired
    AppProperties appProperties;

    @Autowired
    private RouteProcOSRM routeProc;

//    @Test
//    void procTwt() {
//
//    }
}
