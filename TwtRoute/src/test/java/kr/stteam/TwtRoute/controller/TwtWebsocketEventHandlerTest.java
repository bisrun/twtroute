/**
 * ref page:
 * https://programmer.help/blogs/spring-boot-development-series-experience-in-developing-websocket.html
 */
package kr.stteam.TwtRoute.controller;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import kr.stteam.TwtRoute.AppProperties;
import kr.stteam.TwtRoute.repository.MemoryTwtRepository;
import kr.stteam.TwtRoute.repository.TwtRepository;
import kr.stteam.TwtRoute.service.*;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
//@ExtendWith(SpringExtension.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

public class TwtWebsocketEventHandlerTest {
    private static Logger logger = LoggerFactory.getLogger(TwtWebsocketEventHandlerTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    TwtService twtService;


    @Test
    void wsTwTripPostRequestTest_noTW() throws Exception {
        //----------------------------------------------
        //given
        ClassPathResource inputResource =
            new ClassPathResource("/json/vv04_simple_no_tw.json");
            //new ClassPathResource("/json/via_200_no_tw.json");
        InputStream inputStream = inputResource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String inputJson = reader.lines().collect(Collectors.joining("\n"));

        TwtWebsocketClientSample myWebSocketClient = new TwtWebsocketClientSample(getUrl("/route/v1/tsptw/websocket"));
        myWebSocketClient.connect();

        //while (!WebSocket.READYSTATE.OPEN.equals(myWebSocketClient.getReadyState())){
        while (!myWebSocketClient.isOpen()) {
            logger.info("WebSocket Client connection, please wait...");
            Thread.sleep(500);
        }

        myWebSocketClient.send(inputJson);
        //myWebSocketClient.send(inputJson.getBytes(StandardCharsets.UTF_8));

        if (myWebSocketClient.latch.await(30000, TimeUnit.MILLISECONDS)) {
            // network, server ??
            //System.out.println("++websocket client recieviced data: " + myWebSocketClient.responseMessage);

        }
        //System.out.println("--websocket client recieviced data: " + myWebSocketClient.responseMessage);
        DocumentContext parse = JsonPath.parse(myWebSocketClient.responseMessage);
        System.out.println("msg: "+myWebSocketClient.responseMessage);

        //then
        assertThat(parse.read("$.solution.routes[0].activities[0].loc_name").toString()).isEqualToIgnoringCase("mappers");
        assertThat(parse.read("$.solution.routes[0].activities[1].loc_name").toString()).isEqualToIgnoringCase("spo-any");
        assertThat(parse.read("$.solution.routes[0].activities[2].loc_name").toString()).isEqualToIgnoringCase("송파역");
        assertThat(parse.read("$.solution.routes[0].activities[3].loc_name").toString()).isEqualToIgnoringCase("석촌역");
        assertThat(parse.read("$.solution.routes[0].activities[4].loc_name").toString()).isEqualToIgnoringCase("송파나루역");
        assertThat(parse.read("$.solution.routes[0].activities[5].loc_name").toString()).isEqualToIgnoringCase("mappers");


    }

    //@Test
    //@Disabled
    void websocketClient() throws Exception {
        TwtWebsocketClientSample myWebSocketClient = new TwtWebsocketClientSample(getUrl("/websocket/v1/sample"));
        //TwtWebsocketClientSample myWebSocketClient = new TwtWebsocketClientSample(new URI("/websocket/v1/sample"));

        myWebSocketClient.connect();

        //while (!WebSocket.READYSTATE.OPEN.equals(myWebSocketClient.getReadyState())){
        while (!myWebSocketClient.isOpen()) {
            logger.info("WebSocket Client connection, please wait...");
            Thread.sleep(500);
        }
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("HEART_CHECK", "Request heartbeat");
//        requestMap.put("KEY1","VALUE1");
//        requestMap.put("KEY2","VALUE2");
//        requestMap.put("KEY3","VALUE3");
        requestMap.put("KEY4", "CLOSE");


        for (String key : requestMap.keySet()) {
            myWebSocketClient.send(requestMap.get(key));
        }

        if (myWebSocketClient.latch.await(3000, TimeUnit.MILLISECONDS)) {
            System.out.println(myWebSocketClient.responseMessage);
            assertThat(myWebSocketClient.responseMessage).contains("echo-server: Request heartbeat");
        }

        //Test onError, onMessage, onClose
        // ... and so on.
        myWebSocketClient.close();
    }

//    @Test
//    public void echo() throws Exception {
//        int count = 4;
//        Flux<String> input = Flux.range(1, count).map(index -> "msg-" + index);
//        ReplayProcessor<Object> output = ReplayProcessor.create(count);
//
//        WebSocketClient client = new StandardWebSocketClient();
//        client.execute(getUrl("/websocket/echo"),
//            session -> session
//                .send(input.map(session::textMessage))
//                .thenMany(session.receive().take(count).map(WebSocketMessage::getPayloadAsText))
//                .subscribeWith(output)
//                .then())
//            .block(Duration.ofMillis(5000));
//
//        assertEquals(input.collectList().block(Duration.ofMillis(5000)), output.collectList().block(Duration.ofMillis(5000)));
//    }

    protected URI getUrl(String path) throws URISyntaxException {
        return new URI("ws://localhost:" + port + path);
    }
}
