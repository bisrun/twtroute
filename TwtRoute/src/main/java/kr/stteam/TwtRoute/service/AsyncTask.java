package kr.stteam.TwtRoute.service;

import kr.stteam.TwtRoute.domain.TwtJobDesc;
import kr.stteam.TwtRoute.domain.TwtTaskItem;
import kr.stteam.TwtRoute.protocol.*;
import kr.stteam.TwtRoute.repository.TwtRepository;
import kr.stteam.TwtRoute.util.UtilCommon;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service("asyncTask")
public class AsyncTask {
    private final TwtService twtService;
    private final TwtRepository twtRepository;
    private ConcurrentLinkedQueue<TwtRequest_Tsptw> requestQueue;

    public AsyncTask(TwtService twtService, TwtRepository twtRepository) {
        this.twtService = twtService;
        this.twtRepository = twtRepository;
        this.requestQueue =  new ConcurrentLinkedQueue<TwtRequest_Tsptw>();
    }

    /**
     * 시뮬레이션 테스트용 함수
     *
     * @param str
     */
    @Async("twtRoute_executor")
    public void twtRoute_executor(String str) {
        // LOG : 시작 입력
        // ...
        System.out.println("==============>>>>>>>>>>>> THREAD START");

        // 내용
        // 내용
        // 내용
        System.out.println("Test Log - " + str);
        // LOG : 종료 입력
        // ...
        System.out.println("==============>>>>>>>>>>>> THREAD END");
    }

    @Async("twtRoute_executor")
    public ListenableFuture<TwtResponse_forAssignJob> assignJob(TwtRequest_Tsptw request) {
        TwtResponse_forAssignJob result = new TwtResponse_forAssignJob();
        result.setJob_id(request.getJobId());
        result.setStatus(TwtResponse_Base.StatusType.Ok);
        result.setReg_job_time(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())); //현재시간으로 등록시간을 사용
        return new AsyncResult<TwtResponse_forAssignJob>(result);
    }

    @Async("twtRoute_executor")
    //redis(memory)에 처리중 상태을 알린다.
    public void SetResponseToDB(TwtResponse_Tsptw result, String Msg){
        this.twtRepository.setResponseToDB(new TwtResponseWrapper(result, Msg));
    }

    @Async("twtRoute_executor")
    //redis(memory)에 처리중 상태을 알린다.
    public void SetResponseToDB(TwtResponse_forAssignJob result, String Msg){
        this.twtRepository.setResponseToDB(new TwtResponseWrapper(result, Msg));
    }

    @Async("twtRoute_executor")
    //redis(memory)에 처리중 상태을 알린다.
    public ListenableFuture<TwtResponse_Tsptw> GetResponseFromDB(String job_id){
        Optional<TwtResponseWrapper> responseDb = this.twtRepository.getResponseFromDB(job_id);

        if (responseDb.isPresent()){
            TwtResponse_Tsptw result = (TwtResponse_Tsptw) responseDb.get().getResponse();
            return new AsyncResult<TwtResponse_Tsptw>(result);
        }
        return null;
    }
    @Async("twtRoute_executor")
    //redis(memory)에 처리중 상태을 알린다.
    public void PrintResponseDB(){
       this.twtRepository.printDB();
    }

    @Async("twtRoute_executor")
    public ListenableFuture<TwtResponse_Tsptw> procTwtTask(TwtJobDesc twtJobDesc){
        //3. process Job
        TwtResponse_Tsptw result = twtService.procTwt(twtJobDesc);
        return new AsyncResult<TwtResponse_Tsptw>(result);
    }

    public TwtResponse_Tsptw procRequestTask(TwtJobDesc twtJobDesc){
        TwtResponse_Tsptw result = twtService.procTwt(twtJobDesc);
        //SetResponseToDB(result, Constants.Msg_Process_Done);
        return result;
    }
}
