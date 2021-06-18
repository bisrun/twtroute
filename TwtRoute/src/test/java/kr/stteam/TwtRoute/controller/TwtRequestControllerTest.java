package kr.stteam.TwtRoute.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.stteam.TwtRoute.AppProperties;
import kr.stteam.TwtRoute.protocol.OsrmRouteResponseParam_Base;
import kr.stteam.TwtRoute.protocol.TwtResponseParam_Base;
import kr.stteam.TwtRoute.protocol.TwtResponseParam_RouteActivite;
import kr.stteam.TwtRoute.service.RouteProcOSRM;
import kr.stteam.TwtRoute.service.TwtService;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
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
        TwtResponseParam_Base twtResponse = null;
        try {
            twtResponse = mapper.readValue(responseJson, TwtResponseParam_Base.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        logger.info(responseJson);
        //then
        assertNotNull(twtResponse);
        ArrayList<TwtResponseParam_RouteActivite> activites = twtResponse.getSolution().getRoutes().get(0).getActivities();

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
