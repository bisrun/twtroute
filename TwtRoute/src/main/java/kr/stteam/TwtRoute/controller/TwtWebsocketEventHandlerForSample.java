/**
 * ref page: https://www.programmersought.com/article/9614475358/
 * http://localhost:8088/websocket/sample
 */
package kr.stteam.TwtRoute.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArraySet;


@ServerEndpoint(value = "/websocket/{api_version}/sample")
@Component
public class TwtWebsocketEventHandlerForSample {
    // Static variable, used to record the current number of online connections. It should be designed to be thread safe.
    private static int onlineCount = 0;

    // The thread-safe Set of the concurrent package is used to store the MyWebSocket object corresponding to each client.
    private static CopyOnWriteArraySet<TwtWebsocketEventHandlerForSample> webSocketSet = new CopyOnWriteArraySet<TwtWebsocketEventHandlerForSample>();

    // Connection session with a client, you need to send data to the client
    private Session session;

    public ArrayList<String> onMessageList = new ArrayList<String>();

    /**
     * Connection established method of successful call
     */

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("api_version") String version) {
        //@PathParam 은 웹소켓에서 사용하는 @PathVariables
        this.session = session;
        webSocketSet.add(this); //Add to set
        addOnlineCount(); // online number plus 1
        System.out.println("There is a new connection to join! The current number of people online" + getOnlineCount());
        System.out.println("version" + version);
//        try {
//            sendMessage("haha");
//        } catch (IOException e) {
//            System.out.println("IO exception");
//        }
    }


    /**
     * Connection close call method
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this); //remove from set
        subOnlineCount(); //Online number minus 1
        System.out.println("There is a connection closed! The current online number is " + getOnlineCount());
    }

    /**
     * Method called after receiving the client message
     *
     * @param message Message sent by the client
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("message from client:" + message);
        String serverMessage = "echo-server: "+message;

        // Mass message
        for (TwtWebsocketEventHandlerForSample item : webSocketSet) {
            try {
                item.sendMessage(serverMessage);
                onMessageList.add(serverMessage);
                if(onMessageList.size() > 10){
                    onMessageList.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Called when an error occurs
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("An error has occurred");
        error.printStackTrace();
    }

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
        //this.session.getAsyncRemote().sendText(message);
    }

    /**
     * Bulk custom message
     */
    public static void sendInfo(String message) throws IOException {
        for (TwtWebsocketEventHandlerForSample item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                continue;
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        TwtWebsocketEventHandlerForSample.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        TwtWebsocketEventHandlerForSample.onlineCount--;
    }

}
