package kr.stteam.TwtRoute.service;

import kr.stteam.TwtRoute.AppProperties;
import kr.stteam.TwtRoute.domain.TwtJobDesc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled
class RouteProcOSRMTest {

    @Autowired
    AppProperties appProperties;

    //@Autowired
   // private MockRestServiceServer server;
    @MockBean
    TwtJobDesc mockTwtJobDesc;

    @Autowired
    private RouteProcOSRM routeProc;

    @BeforeEach
    public void beforeEach(){
    }

    @Test
    void requestTripMatrixTest() {
        //given
        // 맵퍼스->스포애니->송파역->석촌역->송파나루역->
        String viaPoint = "127.1145019,37.5086800;127.1121007,37.5098758;127.1078362,37.5053189;127.1128161,37.4993756;127.1162522,37.5078703";

        //when
        String responseJson = routeProc.requestTripMatrix(new StringBuffer(viaPoint));

        //then
        assertThat(responseJson).contains("\"code\":\"Ok\"");
        //assertThat(responseJson).contains("\"distances\":");
        assertThat(responseJson).contains("\"durations\":");
    }

//    @Test
//    void setTripMatrixInResult() {
//
//
//
//    }

    @Test
    void requestRouteGeometryTest() {

        //given
        // 맵퍼스->스포애니->송파역->석촌역->송파나루역->맵퍼스
        String viaPoint = "127.1145019,37.5086800;127.1162522,37.5078703;127.1128161,37.4993756;127.1078362,37.5053189;127.1121007,37.5098758;127.1145019,37.5086800";

        //when
        when(mockTwtJobDesc.GetOrderedWaypoint()).thenReturn(viaPoint);
        String responseJson = routeProc.requestRouteGeometry(mockTwtJobDesc);

        //then
        assertThat(responseJson).contains("\"code\":\"Ok\"");
        assertThat(responseJson).contains("\"waypoints\":");
    }

}
