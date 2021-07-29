/**
 * ref page: https://www.programmersought.com/article/9614475358/
 * http://localhost:8088/websocket/sample
 */
package kr.stteam.TwtRoute.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.stteam.TwtRoute.domain.TwtJobDesc;
import kr.stteam.TwtRoute.protocol.TwtResponse_Base;
import kr.stteam.TwtRoute.protocol.TwtResponse_Error;
import kr.stteam.TwtRoute.service.AsyncConfig;
import kr.stteam.TwtRoute.service.AsyncTask;
import kr.stteam.TwtRoute.service.TwtService;
import kr.stteam.TwtRoute.util.BeanUtils;
import kr.stteam.TwtRoute.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;


import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArraySet;


@ServerEndpoint(value = "/route/{api_version}/tsptw/websocket")
@Component

public class TwtWebsocketEventHandler {
    // Static variable, used to record the current number of online connections. It should be designed to be thread safe.
    private static int onlineCount = 0;

    // The thread-safe Set of the concurrent package is used to store the MyWebSocket object corresponding to each client.
    private static CopyOnWriteArraySet<TwtWebsocketEventHandler> webSocketSet = new CopyOnWriteArraySet<TwtWebsocketEventHandler>();

    // Connection session with a client, you need to send data to the client
    private Session session;

    private AsyncTask asyncTask;
    private AsyncConfig asyncConfig;

    /**
     * Connection established method of successful call
     */

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("api_version") String version) {
        //@PathParam 은 웹소켓에서 사용하는 @PathVariables

        //session.setMaxTextMessageBufferSize(1024*1024);
        this.session = session;
        webSocketSet.add(this); //Add to set
        addOnlineCount(); // online number plus 1
        System.out.println("There is a new connection to join! The current number of people online" + getOnlineCount());
        System.out.println("version: " + version);

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
        //System.out.println("message from client:" + message);
        String serverMessage = null;

        TwtJobDesc jobDesc = TwtJobDesc.create(message);
        TwtResponse_Base result = null;
        ObjectMapper objectMapper = new ObjectMapper();

        try {


            if(this.asyncConfig == null){

                //this.twtService = (TwtService) context.getBean("twtService");
                //this.twtService = (TwtService) context.getBean(TwtService.class);
                this.asyncConfig= (AsyncConfig) BeanUtils.getBean("asyncConfig");
                this.asyncTask= (AsyncTask) BeanUtils.getBean("asyncTask");

            }

            // 등록 가능 여부 체크
            if (asyncConfig.isTaskExecute()) {
                result = asyncTask.procRequestTask(jobDesc);
                serverMessage = objectMapper.writeValueAsString(result);
            } else {
                System.out.println("==============>>>>>>>>>>>> THREAD 개수 초과");
            }
        } catch (TaskRejectedException | JsonProcessingException e) {
            // TaskRejectedException : 개수 초과시 발생
            System.out.println("==============>>>>>>>>>>>> THREAD ERROR");
            System.out.println("TaskRejectedException : 등록 개수 초과");
            System.out.println("==============>>>>>>>>>>>> THREAD END");
        }


        if(result == null){
            result = new TwtResponse_Error(TwtResponse_Base.StatusType.Fail, Constants.Msg_Err1);
            try {
                serverMessage = objectMapper.writeValueAsString(result);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }


        // Mass message
        try {

            session.getBasicRemote().sendText(serverMessage);
            //session.getBasicRemote().sendText("CLOSE");
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
        for (TwtWebsocketEventHandler item : webSocketSet) {
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
        TwtWebsocketEventHandler.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        TwtWebsocketEventHandler.onlineCount--;
    }

}
