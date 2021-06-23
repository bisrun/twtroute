package kr.stteam.TwtRoute.controller;

import kr.stteam.TwtRoute.domain.TwtTaskItem;
import kr.stteam.TwtRoute.protocol.TwtRequest_Base;
import kr.stteam.TwtRoute.protocol.TwtRequest_Service;
import kr.stteam.TwtRoute.protocol.TwtResponse_Base;
import kr.stteam.TwtRoute.service.TwtService;
import kr.stteam.TwtRoute.util.UtilCommon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class TwtRequestController {

    private final TwtService twtService;

    @Autowired
    public TwtRequestController(TwtService twtService){this.twtService = twtService;}

    @GetMapping("/twtrip")
    public String twTripGetRequest(Model model){
        return "good morning";
    }


    @PostMapping("/twtrip")
    public TwtResponse_Base twTripPostRequest(@RequestBody String fullJson){
        TwtRequest_Base request = TwtRequestParameter.parseParam(fullJson);

        ArrayList<TwtTaskItem> tasklist = new ArrayList<TwtTaskItem>();
        setJobList(tasklist, request);
        TwtResponse_Base result = twtService.procTwt(tasklist, request);
        return result ;
    }

    @PostMapping("/twtrip/v016")
    public TwtResponse_Base twTripPostRequest016(@RequestBody String fullJson){
        TwtRequest_Base request = TwtRequestParameter.parseParam(fullJson);

        ArrayList<TwtTaskItem> tasklist = new ArrayList<TwtTaskItem>();
        setJobList(tasklist, request);
        TwtResponse_Base result = twtService.procTwt(tasklist, request);
        return result ;
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
