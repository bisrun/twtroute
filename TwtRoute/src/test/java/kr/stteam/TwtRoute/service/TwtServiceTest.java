package kr.stteam.TwtRoute.service;

import kr.stteam.TwtRoute.AppProperties;
import kr.stteam.TwtRoute.domain.TwtJobDesc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = AppProperties.class)
@TestPropertySource("classpath:application.properties")
class TwtServiceTest {

    @MockBean
    TwtJobDesc mockTwtJobDesc;

    @Autowired
    AppProperties appProperties;

    @Autowired
    private RouteProcOSRM routeProc;

    @BeforeEach
    public void beforeEach(){
        routeProc =new RouteProcOSRM(appProperties);
    }


//    @Test
//    void procTwt() {
//
//    }
}
