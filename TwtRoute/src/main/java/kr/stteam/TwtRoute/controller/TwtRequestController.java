package kr.stteam.TwtRoute.controller;

import kr.stteam.TwtRoute.domain.TwtTaskItem;
import kr.stteam.TwtRoute.protocol.TwtRequest_Base;
import kr.stteam.TwtRoute.protocol.TwtRequest_Service;
import kr.stteam.TwtRoute.protocol.TwtResponseS1_Base;
import kr.stteam.TwtRoute.protocol.TwtResponse_Base;
import kr.stteam.TwtRoute.service.TwtService;
import kr.stteam.TwtRoute.util.UtilCommon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;


@RestController
public class TwtRequestController {

    private final TwtService twtService;

    @Autowired
    public TwtRequestController(TwtService twtService){this.twtService = twtService;}


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
        TwtRequest_Base request = TwtRequestParameter.parseParam(fullJson);

        ArrayList<TwtTaskItem> tasklist = new ArrayList<TwtTaskItem>();
        setJobList(tasklist, request);
        TwtResponse_Base result = twtService.procTwt(tasklist, request);
        return result ;
    }


    @PostMapping("/route/{api_version}/tsptw/registjob")
    public TwtResponseS1_Base registTsptwJob(@RequestBody String fullJson,
                                              @PathVariable("api_version") String version,
                                              @RequestParam(value = "auth_id", required = true) String auth_id,
                                              @RequestParam(value = "device_id", required = true) String device_id){
        TwtRequest_Base request = TwtRequestParameter.parseParam(fullJson);

        ArrayList<TwtTaskItem> tasklist = new ArrayList<TwtTaskItem>();
        setJobList(tasklist, request);

        // 개발해야함
        return null ;
    }

    @GetMapping("/route/{api_version}/tsptw/solution/{job_id}")
    public TwtResponse_Base getTsptwJobSolutionN( @PathVariable("api_version") String version,
                  @PathVariable("job_id") String job_id,
                  @RequestParam(value = "auth_id", required = true) String auth_id,
                  @RequestParam(value = "device_id", required = true) String device_id){
        // 개발해야함
        return null ;
    }

    private void setJobList(ArrayList<TwtTaskItem> tasklist, TwtRequest_Base request) {
        int index = 0;
        for(TwtRequest_Service item : request.getServices() ){
            TwtTaskItem task = new TwtTaskItem();
            task.task_id = item.getTask_id();
            task.x = item.getLoc_coord().get(0);
            task.y = item.getLoc_coord().get(1);
            task.tm_service = item.getService_time();
            task.index = index;
            task.poi_name = item.getName();
            if( item.getTime_window() != null)
            {
                // 문자열 parsing 시에 exception throw해야함.
                task.tw_req_start = item.getTime_window().get(0);
                task.tw_req_end =  item.getTime_window().get(1);
                task.tw_req = 1;
            }
            tasklist.add(task);
            index ++ ;

        }
    }

}
