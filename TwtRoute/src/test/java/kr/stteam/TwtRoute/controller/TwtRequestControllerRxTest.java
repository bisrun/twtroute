package kr.stteam.TwtRoute.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.stteam.TwtRoute.AppProperties;
import kr.stteam.TwtRoute.protocol.TwtResponse_RouteActivity;
import kr.stteam.TwtRoute.protocol.TwtResponse_Tsptw;
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
import java.util.ArrayList;
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
    void twTripPostRequestTest_noTW() throws IOException {

        //----------------------------------------------
        //given
        ClassPathResource inputResource = new ClassPathResource("/json/vv04_simple_no_tw.json");
        InputStream inputStream = inputResource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))  ;
        String inputJson = reader.lines().collect(Collectors.joining("\n"));

        //when
        webTestClient.method(HttpMethod.POST).uri("/route/v1/tsptw/request")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(inputJson), String.class)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            // then
            .jsonPath("$.solution.routes[0].activities[0].loc_name").isEqualTo("mappers")
            .jsonPath("$.solution.routes[0].activities[1].loc_name").isEqualTo("spo-any")
            .jsonPath("$.solution.routes[0].activities[2].loc_name").isEqualTo("송파역")
            .jsonPath("$.solution.routes[0].activities[3].loc_name").isEqualTo("석촌역")
            .jsonPath("$.solution.routes[0].activities[4].loc_name").isEqualTo("송파나루역")
            .jsonPath("$.solution.routes[0].activities[5].loc_name").isEqualTo("mappers");


    }


    @Test
    void twTripPostRequestTest_withTW() throws IOException {
        //----------------------------------------------
        //given
        ClassPathResource inputResource = new ClassPathResource("/json/vv04_simple_tw.json");
        InputStream inputStream = inputResource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))  ;
        String inputJson = reader.lines().collect(Collectors.joining("\n"));


        //when
        webTestClient.method(HttpMethod.POST).uri("/route/v1/tsptw/request")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(inputJson), String.class)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            // then
            .jsonPath("$.solution.routes[0].activities[0].loc_name").isEqualTo("mappers")
            .jsonPath("$.solution.routes[0].activities[1].loc_name").isEqualTo("송파나루역")
            .jsonPath("$.solution.routes[0].activities[2].loc_name").isEqualTo("석촌역")
            .jsonPath("$.solution.routes[0].activities[3].loc_name").isEqualTo("송파역")
            .jsonPath("$.solution.routes[0].activities[4].loc_name").isEqualTo("spo-any")
            .jsonPath("$.solution.routes[0].activities[5].loc_name").isEqualTo("mappers");
    }


    @Test
    void twTripPostRequestTest_withoutEndPoint() throws IOException {
        //----------------------------------------------
        //given
        ClassPathResource inputResource3 = new ClassPathResource("/json/vv04_simple_no_tw_no_end.json");
        InputStream inputStream3 = inputResource3.getInputStream();
        BufferedReader reader3 = new BufferedReader(new InputStreamReader(inputStream3, "UTF-8"))  ;
        String inputJson3 = reader3.lines().collect(Collectors.joining("\n"));

        //when
        webTestClient.method(HttpMethod.POST).uri("/route/v1/tsptw/request?auth_id=00x0000x0000&device_id=dev001001")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(inputJson3), String.class)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            // then
            .jsonPath("$.solution.routes[0].activities[0].loc_name").isEqualTo("mappers")
            .jsonPath("$.solution.routes[0].activities[1].loc_name").isEqualTo("spo-any")
            .jsonPath("$.solution.routes[0].activities[2].loc_name").isEqualTo("송파역")
            .jsonPath("$.solution.routes[0].activities[3].loc_name").isEqualTo("석촌역")
            .jsonPath("$.solution.routes[0].activities[4].loc_name").isEqualTo("송파나루역")
            .jsonPath("$.solution.routes[0].activities[5].loc_name").isEqualTo("송파나루역");

    }

    @Test
    void twTripPostRequestTest_DiffStartPtEndPt() throws IOException {
        //----------------------------------------------
        //given, start :mappers, end:석촌역
        ClassPathResource inputResource = new ClassPathResource("/json/vv04_simple_no_tw_diff_start_end.json");
        InputStream inputStream = inputResource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))  ;
        String inputJson = reader.lines().collect(Collectors.joining("\n"));


        //when
        webTestClient.method(HttpMethod.POST).uri("/route/v1/tsptw/request")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(inputJson), String.class)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            // then
            .jsonPath("$.solution.routes[0].activities[0].loc_name").isEqualTo("mappers")
            .jsonPath("$.solution.routes[0].activities[1].loc_name").isEqualTo("송파나루역")
            .jsonPath("$.solution.routes[0].activities[2].loc_name").isEqualTo("spo-any")
            .jsonPath("$.solution.routes[0].activities[3].loc_name").isEqualTo("송파역")
            .jsonPath("$.solution.routes[0].activities[4].loc_name").isEqualTo("석촌역");
    }
}
