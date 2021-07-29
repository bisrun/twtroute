package kr.stteam.TwtRoute.controller;

import kr.stteam.TwtRoute.domain.TwtJobDesc;
import kr.stteam.TwtRoute.protocol.*;
import kr.stteam.TwtRoute.service.*;
import kr.stteam.TwtRoute.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
//@RequestMapping(path = "/route/{api_version}/tsptw")
public class TwtRequestController {

    @Autowired
    private AsyncTask asyncTask;

    /** AsyncConfig */
    @Resource(name = "asyncConfig")
    private AsyncConfig asyncConfig;

    /**
     * bloking mode, get tsptw result funtion
     *
     * it is possible to wait result more than 60 second.
     * this is recommended for debugging.
     * @param fullJson
     * @return
     */
    @PostMapping("/route/{api_version}/tsptw/request")
    public TwtResponse_Base getTsptwJobSolutionB(@RequestBody String fullJson,
                                                 @PathVariable("api_version") String version){
        //TwtRequest_Tsptw request = TwtRequestMapper.parseParam(fullJson);
        TwtJobDesc jobDesc = TwtJobDesc.create(fullJson);
        TwtResponse_Base result = null;
        try {
            // 등록 가능 여부 체크
            if (asyncConfig.isTaskExecute()) {
                result = asyncTask.procRequestTask(jobDesc);
            } else {
                System.out.println("==============>>>>>>>>>>>> THREAD 개수 초과");
            }
        } catch (TaskRejectedException e) {
            // TaskRejectedException : 개수 초과시 발생
            System.out.println("==============>>>>>>>>>>>> THREAD ERROR");
            System.out.println("TaskRejectedException : 등록 개수 초과");
            System.out.println("==============>>>>>>>>>>>> THREAD END");
        }

        if(result == null){
            result = new TwtResponse_Error(TwtResponse_Base.StatusType.Fail, Constants.Msg_Err1);
        }

        return result;
    }


    @PostMapping("/route/{api_version}/tsptw/registjob")
    public TwtResponse_Base registTsptwJob(@RequestBody String fullJson,
                                              @PathVariable("api_version") String version,
                                              @RequestParam(value = "auth_id", required = true) String auth_id,
                                              @RequestParam(value = "device_id", required = true) String device_id) throws IOException {

        //TwtRequest_Tsptw request = TwtRequestMapper.parseParam(fullJson);
        TwtJobDesc jobDesc = TwtJobDesc.create(fullJson);

        if (jobDesc == null) { //parsing fail 처리
            return new TwtResponse_Error(TwtResponse_Base.StatusType.Fail, Constants.Msg_Err0);
        }
        if (jobDesc.requestParam == null) { //parsing fail 처리
            return new TwtResponse_Error(TwtResponse_Base.StatusType.Fail, Constants.Msg_Err0);
        }

        TwtResponse_Base result = null;
        try {
            // 등록 가능 여부 체크
            if (asyncConfig.isTaskExecute()) {
                //비동기 함수지만, assign result (특히 jobId)를 리턴해줘야 되므로
                //여기선 그냥 get을 사용하여 비동기를 무시하고 결과를 받아온다.
                //대신 결과에 대한 result의 db insert는 비동기로 진행한다.
                result = asyncTask.assignJob(jobDesc.requestParam).get();

                if(result != null){
                    asyncTask.SetResponseToDB((TwtResponse_forAssignJob) result, Constants.Msg_Process_Ing);

                    //twtRoute 작업을 지시한다
                    asyncTask.procTwtTask(jobDesc).addCallback((twtRouteResult) -> {
                        //완성된 결과를 DB에 삽입한다.
                        asyncTask.SetResponseToDB((TwtResponse_Tsptw)twtRouteResult, Constants.Msg_Process_Done);

                    }, (e) -> {
                        e.printStackTrace();
                    });;

                }
            } else {
                System.out.println("==============>>>>>>>>>>>> THREAD 개수 초과");
            }
        } catch (TaskRejectedException e) {
            // TaskRejectedException : 개수 초과시 발생
            System.out.println("==============>>>>>>>>>>>> THREAD ERROR");
            System.out.println("TaskRejectedException : 등록 개수 초과");
            System.out.println("==============>>>>>>>>>>>> THREAD END");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(result == null){
            result = new TwtResponse_Error(TwtResponse_Base.StatusType.Fail, Constants.Msg_Err1);;
	    }
	    return result;
    }

    @GetMapping("/route/{api_version}/tsptw/print")
    public TwtResponse_Base twTripPrintStoreSolution(){
        try {
            // 등록 가능 여부 체크
            if (asyncConfig.isTaskExecute()) {
                asyncTask.PrintResponseDB();
            } else {
                System.out.println("==============>>>>>>>>>>>> THREAD 개수 초과");
            }
        } catch (TaskRejectedException e) {
            // TaskRejectedException : 개수 초과시 발생
            System.out.println("==============>>>>>>>>>>>> THREAD ERROR");
            System.out.println("TaskRejectedException : 등록 개수 초과");
            System.out.println("==============>>>>>>>>>>>> THREAD END");
        }
        return new TwtResponse_Error(TwtResponse_Base.StatusType.Ok, "Test Print Method");
    }


    @GetMapping("/route/{api_version}/tsptw/solution/{job_id}")
    public TwtResponse_Base getTsptwJobSolutionN( @PathVariable("api_version") String version,
                  @PathVariable("job_id") String job_id,
                  @RequestParam(value = "auth_id", required = true) String auth_id,
                  @RequestParam(value = "device_id", required = true) String device_id){
        // 개발해야함
        TwtResponse_Base result = null;

        try {
            // 등록 가능 여부 체크
            if (asyncConfig.isTaskExecute()) {

                result = asyncTask.GetResponseFromDB(job_id).get();

                if(result == null){
                    result = new TwtResponse_Error(TwtResponse_Base.StatusType.Fail, Constants.Msg_Err2);;
                }
            }
        } catch (TaskRejectedException e) {
            // TaskRejectedException : 개수 초과시 발생
            System.out.println("==============>>>>>>>>>>>> THREAD ERROR");
            System.out.println("TaskRejectedException : 등록 개수 초과");
            System.out.println("==============>>>>>>>>>>>> THREAD END");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(result == null){
            result = new TwtResponse_Error(TwtResponse_Base.StatusType.Fail, Constants.Msg_Err1);;
        }
        return result;
    }
}
