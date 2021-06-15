package kr.stteam.TwtRoute.controller;

import kr.stteam.TwtRoute.protocol.TwtRequestParam_BaseData;
import kr.stteam.TwtRoute.protocol.TwtRequestParam_ServiceItem;
import kr.stteam.TwtRoute.protocol.TwtResponseParam_Base;
import kr.stteam.TwtRoute.service.TwtTaskItem;
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
    public TwtResponseParam_Base twTripPostRequest(@RequestBody String fullJson){
        TwtRequestParam_BaseData request = TwtRequestParameter.parseParam(fullJson);

        ArrayList<TwtTaskItem> tasklist = new ArrayList<TwtTaskItem>();
        setJobList(tasklist, request);
        TwtResponseParam_Base result = twtService.procTwt(tasklist, request);

        return result ;
    }

    private void setJobList(ArrayList<TwtTaskItem> tasklist, TwtRequestParam_BaseData request) {
        int index = 0;
        for(TwtRequestParam_ServiceItem item : request.getServices() ){
            TwtTaskItem task = new TwtTaskItem();
            task.task_id = item.getId();
            task.x = item.getPos().get(0);
            task.y = item.getPos().get(1);
            task.tm_service = item.getSvctime();
            task.index = index;
            task.poi_name = item.getName();
            if( item.getTimewindow() != null)
            {
                // 문자열 parsing 시에 exception throw해야함.
                task.tw_req_start = UtilCommon.convHMtoSec(item.getTimewindow().get(0));
                task.tw_req_end =  UtilCommon.convHMtoSec(item.getTimewindow().get(1));
                task.tw_req = 1;
            }

            tasklist.add(task);

            index ++ ;

        }
    }

}
