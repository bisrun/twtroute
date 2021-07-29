/**
 * ref page: https://www.programmersought.com/article/9614475358/
 * http://localhost:8088/websocket/sample
 */
package kr.stteam.TwtRoute.controller;

import ch.qos.logback.core.util.ContextUtil;
import kr.stteam.TwtRoute.domain.TwtJobDesc;
import kr.stteam.TwtRoute.protocol.TwtResponse_Base;
import kr.stteam.TwtRoute.protocol.TwtResponse_Error;
import kr.stteam.TwtRoute.protocol.TwtResponse_Tsptw;
import kr.stteam.TwtRoute.service.AsyncConfig;
import kr.stteam.TwtRoute.service.AsyncTask;
import kr.stteam.TwtRoute.service.TwtService;
import kr.stteam.TwtRoute.util.BeanUtils;
import kr.stteam.TwtRoute.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;


@ServerEndpoint(value = "/route/websocket")
@Controller
public class TwtWebsocketEventHandler4 {
    // Static variable, used to record the current number of online connections. It should be designed to be thread safe.
    private static int onlineCount = 0;

    // The thread-safe Set of the concurrent package is used to store the MyWebSocket object corresponding to each client.
    private static CopyOnWriteArraySet<TwtWebsocketEventHandler4> webSocketSet = new CopyOnWriteArraySet<TwtWebsocketEventHandler4>();

    // Connection session with a client, you need to send data to the client
    private Session session;

    private TwtService twtService = null;


    /**
     * Connection established method of successful call
     */

    @OnOpen
    public void onOpen(Session session) {
        //@PathParam 은 웹소켓에서 사용하는 @PathVariables
        this.session = session;
        webSocketSet.add(this); //Add to set
        addOnlineCount(); // online number plus 1
        System.out.println("There is a new connection to join! The current number of people online" + getOnlineCount());
        //System.out.println("version" + version);

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


        if(this.twtService == null){

            //this.twtService = (TwtService) context.getBean("twtService");
            //this.twtService = (TwtService) context.getBean(TwtService.class);
            this.twtService= (TwtService) BeanUtils.getBean("twtService");


        }

        TwtJobDesc jobDesc = TwtJobDesc.create(message);
        TwtResponse_Base result = twtService.procTwt(jobDesc);;

        if(result == null){
            result = new TwtResponse_Error(TwtResponse_Base.StatusType.Fail, Constants.Msg_Err1);
        }

        // Mass message
        try {
            session.getBasicRemote().sendText(result.toString());
            session.getBasicRemote().sendText("CLOSE");
        } catch (IOException e) {
            e.printStackTrace();
        }

//        for (TwtWebsocketEventHandler2 item : webSocketSet) {
//            try {
//                item.sendMessage(serverMessage);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

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
        for (TwtWebsocketEventHandler4 item : webSocketSet) {
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
        TwtWebsocketEventHandler4.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        TwtWebsocketEventHandler4.onlineCount--;
    }

}
