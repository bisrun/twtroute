package kr.stteam.TwtRoute.service;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.CountDownLatch;

public class TwtWebsocketClientSample extends WebSocketClient {
    private static Logger log = LoggerFactory.getLogger(TwtWebsocketClientSample.class);
    public String responseMessage;
    public CountDownLatch latch = new CountDownLatch(1);;

    public TwtWebsocketClientSample(URI uri){
        super(uri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        log.info("Client connection successful");
    }

    @Override
    public void onMessage(String s) {
        log.info("Received message from server to client :"+s);

        if( s.equalsIgnoreCase("complete")){
            latch.countDown();
        }  else {
            responseMessage = s;
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        log.info("Client closed successfully");

    }

    @Override
    public void onError(Exception e) {
        log.error("Client error");
    }
}
