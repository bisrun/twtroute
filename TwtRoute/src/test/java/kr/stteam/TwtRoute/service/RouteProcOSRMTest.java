package kr.stteam.TwtRoute.service;

import kr.stteam.TwtRoute.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(RouteProcOSRM.class)
class RouteProcOSRMTest {

    @Autowired
    AppProperties appProperties;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private RouteProcOSRM routeProc;

    @BeforeEach
    public void beforeEach(){
    }

    @Test
    void requestTripMatrix() {

        server.expect(requestTo("/table/v1/car/"))
            .andRespond(withSuccess(new ClassPathResource("/json/matrix_result.json", getClass()), MediaType.APPLICATION_JSON));
    }

    @Test
    void setTripMatrixInResult() {
    }

    @Test
    void requestRouteGeometry() {
    }

    @Test
    void setRouteGeometryInResult() {
    }
}
