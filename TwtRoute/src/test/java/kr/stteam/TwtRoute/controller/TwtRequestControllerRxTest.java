package kr.stteam.TwtRoute.controller;

import kr.stteam.TwtRoute.AppProperties;
import kr.stteam.TwtRoute.service.RouteProcOSRM;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureWebTestClient
//@WebFluxTest(TwtRequestController.class)
class TwtRequestControllerRxTest {
    private static Logger logger = LoggerFactory.getLogger(RouteProcOSRM.class);

    @Autowired
    AppProperties appProperties;

    @Autowired
    WebTestClient webTestClient;

    @Test
    void twTripPostRequestTest() throws IOException {

        //given
        ClassPathResource inputResource = new ClassPathResource("/json/vv04_simple_no_tw.json");
        InputStream inputStream = inputResource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))  ;
        String inputJson = reader.lines().collect(Collectors.joining("\n"));

        //when
        webTestClient.method(HttpMethod.POST).uri("/twtrip")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(inputJson), String.class)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.solution.routes[0].activities[0].loc_name").isEqualTo("mappers")
            .jsonPath("$.solution.routes[0].activities[1].loc_name").isEqualTo("spo-any")
            .jsonPath("$.solution.routes[0].activities[2].loc_name").isEqualTo("송파역")
            .jsonPath("$.solution.routes[0].activities[3].loc_name").isEqualTo("석촌역")
            .jsonPath("$.solution.routes[0].activities[4].loc_name").isEqualTo("송파나루역")
            .jsonPath("$.solution.routes[0].activities[5].loc_name").isEqualTo("mappers");

        //given
        inputResource = new ClassPathResource("/json/vv04_simple_tw.json");
        inputStream = inputResource.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))  ;
        inputJson = reader.lines().collect(Collectors.joining("\n"));

        //when
        webTestClient.method(HttpMethod.POST).uri("/twtrip")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(inputJson), String.class)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.solution.routes[0].activities[0].loc_name").isEqualTo("mappers")
            .jsonPath("$.solution.routes[0].activities[1].loc_name").isEqualTo("송파나루역")
            .jsonPath("$.solution.routes[0].activities[2].loc_name").isEqualTo("석촌역")
            .jsonPath("$.solution.routes[0].activities[3].loc_name").isEqualTo("송파역")
            .jsonPath("$.solution.routes[0].activities[4].loc_name").isEqualTo("spo-any")
            .jsonPath("$.solution.routes[0].activities[5].loc_name").isEqualTo("mappers");
//        WebTestClient.ResponseSpec exchange = webTestClient.method(HttpMethod.POST).uri("/twtrip")
//            .contentType(MediaType.APPLICATION_JSON)
//            .body(Mono.just(inputJson), String.class)
//            .exchange();//        ObjectMapper mapper = new ObjectMapper();


//        TwtResponseParam_Base twtResponse = null;
//        try {
//            twtResponse = mapper.readValue(responseJson, TwtResponseParam_Base.class);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//        logger.info(responseJson);
//        //then
//        assertNotNull(twtResponse);
//        ArrayList<TwtResponseParam_RouteActivite> activites = twtResponse.getSolution().getRoutes().get(0).getActivities();
//
//        assertThat( activites.get(0).getLoc_name()).isEqualToIgnoringCase("mappers");
//        assertThat( activites.get(1).getLoc_name()).isEqualToIgnoringCase("spo-any");
//        assertThat( activites.get(2).getLoc_name()).isEqualToIgnoringCase("송파역");
//        assertThat( activites.get(3).getLoc_name()).isEqualToIgnoringCase("석촌역");
//        assertThat( activites.get(4).getLoc_name()).isEqualToIgnoringCase("송파나루역");
//        assertThat( activites.get(5).getLoc_name()).isEqualToIgnoringCase("mappers");
//twtResponse.
//twtResponse.getWaypoints()
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
