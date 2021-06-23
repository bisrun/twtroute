package kr.stteam.TwtRoute.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.stteam.TwtRoute.AppProperties;
import kr.stteam.TwtRoute.protocol.TwtResponse_Base;
import kr.stteam.TwtRoute.protocol.TwtResponse_RouteActivity;
import kr.stteam.TwtRoute.service.RouteProcOSRM;
import org.junit.jupiter.api.Disabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Disabled
class TwtRequestControllerTest {
    private static Logger logger = LoggerFactory.getLogger(RouteProcOSRM.class);

    @Autowired
    AppProperties appProperties;

    @Autowired
    TestRestTemplate testRestTemplate;

    //@Test
    void twTripPostRequestTest() throws IOException {

        //given
        ClassPathResource inputResource = new ClassPathResource("/json/vv04_simple_no_tw.json");
        InputStream inputStream = inputResource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))  ;
        String inputJson = reader.lines().collect(Collectors.joining("\n"));

        //when
        String responseJson = testRestTemplate.postForObject("/twtrip", inputJson, String.class);

        ObjectMapper mapper = new ObjectMapper();
        TwtResponse_Base twtResponse = null;
        try {
            twtResponse = mapper.readValue(responseJson, TwtResponse_Base.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        logger.info(responseJson);
        //then
        assertNotNull(twtResponse);
        ArrayList<TwtResponse_RouteActivity> activites = twtResponse.getSolution().getRoutes().get(0).getActivities();

        assertThat( activites.get(0).getLoc_name()).isEqualToIgnoringCase("mappers");
        assertThat( activites.get(1).getLoc_name()).isEqualToIgnoringCase("spo-any");
        assertThat( activites.get(2).getLoc_name()).isEqualToIgnoringCase("송파역");
        assertThat( activites.get(3).getLoc_name()).isEqualToIgnoringCase("석촌역");
        assertThat( activites.get(4).getLoc_name()).isEqualToIgnoringCase("송파나루역");
        assertThat( activites.get(5).getLoc_name()).isEqualToIgnoringCase("mappers");

        //twtResponse.

        //twtResponse.getWaypoints()


            ;
//        webTestClient.method(HttpMethod.POST).uri("/twtroute")
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(inputJson)
//            .exchange()
//            .expectStatus().isOk()
//            .expectBody(String.class)
//            //.isEqualTo("");
        ;


        //then

    }
}
