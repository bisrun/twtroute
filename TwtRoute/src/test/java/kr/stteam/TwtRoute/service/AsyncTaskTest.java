package kr.stteam.TwtRoute.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.stteam.TwtRoute.AppProperties;
import kr.stteam.TwtRoute.domain.TwtJobDesc;
import kr.stteam.TwtRoute.protocol.*;
import kr.stteam.TwtRoute.repository.MemoryTwtRepository;
import kr.stteam.TwtRoute.repository.TwtRepository;
import kr.stteam.TwtRoute.util.Constants;
import kr.stteam.TwtRoute.util.UtilCommon;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class AsyncTaskTest {
    private static Logger logger = LoggerFactory.getLogger(AsyncTaskTest.class);
    @Autowired
    AppProperties appProperties;
    @Autowired
    RouteProcOSRM routeProcOSRM;

    private AsyncTask asyncTask;
    @Autowired TwtService twtService;
    /** AsyncConfig */
    @Resource(name = "asyncConfig")
    private AsyncConfig asyncConfig;

    TwtRepository twtRepository;

    @BeforeEach
    public void beforeEach(){
        twtRepository = new MemoryTwtRepository();
        twtService = new TwtService(appProperties, routeProcOSRM);
        asyncTask = new AsyncTask(twtService, twtRepository);
    }

    @AfterEach()
    public void afterEach() {
        twtRepository.clearStore();
    }

    @Test
    void assignJob() throws IOException, ExecutionException, InterruptedException {
        //given
        ClassPathResource inputResource = new ClassPathResource("/json/vv04_simple_no_tw.json");
        InputStream inputStream = inputResource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))  ;
        String inputJson = reader.lines().collect(Collectors.joining("\n"));

        TwtJobDesc jobDesc = TwtJobDesc.create(inputJson);

        //when
        TwtResponse_forAssignJob assignResult = asyncTask.assignJob(jobDesc.requestParam).get();
        //then
        assertNotNull(assignResult);
        assertThat(assignResult.getStatus().equals("OK"));
        assertNotNull(assignResult.getJob_id());
        assertNotNull(assignResult.getReg_job_time());
    }

    @Test
    /*
     * setResponseToDB Test1. AssignedJob Result Set Test
    */
    void setResponseToDB1() throws ExecutionException, InterruptedException {

        //given
        TwtResponse_forAssignJob assignedResult = new TwtResponse_forAssignJob();
        assignedResult.setJob_id(UtilCommon.defineJobId());
        assignedResult.setStatus(TwtResponse_Base.StatusType.Ok);
        assignedResult.setReg_job_time(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));

        //when - ing??? assinged Result??? ???????????? ???
        asyncTask.SetResponseToDB((TwtResponse_forAssignJob) assignedResult, Constants.Msg_Process_Ing);

        //then - ????????? jobId??? ???????????? ?????? ???????????? ??????????????? ????????? ????????????
        Optional<TwtResponseWrapper> wrapperResult = twtRepository.getResponseFromDB(assignedResult.getJob_id());

        assertThat(wrapperResult.isPresent()); //store ?????? ?????? ???????????? ??????????????????

        TwtResponse_Tsptw result = (TwtResponse_Tsptw) wrapperResult.get().getResponse();

        assertNotNull(result);  //?????? ????????? result??? ?????? ???????????? ????????? ???????????? - status ???

        assertThat(result.getJob_id().equals(assignedResult.getJob_id())); //jobId ??????
        assertThat(result.getStatus().equals(TwtResponse_Base.StatusType.Ok)); //status ??????
        assertThat(wrapperResult.get().getProcessStatus().equals(Constants.Msg_Process_Ing)); //???????????? ????????? ing?????? ???
    }
    @Test
        /*
         * setResponseToDB Test2. TwtResponse_Tsptw Set Test
         */
    void setResponseToDB2() throws ExecutionException, InterruptedException, IOException {
        //given
        ClassPathResource inputResource = new ClassPathResource("/json/TwtResponse_Tsptw_result.json");
        InputStream inputStream = inputResource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))  ;
        String inputJson = reader.lines().collect(Collectors.joining("\n"));

        ObjectMapper mapper = new ObjectMapper();
        TwtResponse_Tsptw twtResponseTsptw = null;
        try {
            twtResponseTsptw = mapper.readValue(inputJson, TwtResponse_Tsptw.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        assertNotNull(twtResponseTsptw);

        //when - ing??? assinged Result??? ???????????? ???
        asyncTask.SetResponseToDB((TwtResponse_Tsptw)twtResponseTsptw, Constants.Msg_Process_Done);
        Thread.sleep(1000); //SetResponseToDB??? ????????? ??????????????? ?????????????????? ??????????????? ???

        //then - ????????? jobId??? ???????????? ?????? ???????????? ??????????????? ????????? ????????????
        TwtResponse_Tsptw result = asyncTask.GetResponseFromDB(twtResponseTsptw.getJob_id()).get(); //request??? ????????? job id  ??? db?????? ??????

        assertNotNull(result);  //?????? ????????? result??? ?????? ???????????? ????????? ???????????? - status ???
        assertThat(twtResponseTsptw).isEqualTo(result); //???????????? ????????? ???????????? ??????????????????.
    }
    @Test
    /*
     * getResponseFromDB Test1. Get AssignedJob Result Get Test
     */
    void getResponseFromDB1() throws ExecutionException, InterruptedException {
        //given
        TwtResponse_forAssignJob assignedResult = new TwtResponse_forAssignJob();
        assignedResult.setJob_id(UtilCommon.defineJobId());
        assignedResult.setStatus(TwtResponse_Base.StatusType.Ok);
        assignedResult.setReg_job_time(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));

        asyncTask.SetResponseToDB((TwtResponse_forAssignJob) assignedResult, Constants.Msg_Process_Ing);

        //when
        TwtResponse_Base result = asyncTask.GetResponseFromDB(assignedResult.getJob_id()).get();

        //then
        assertNotNull(result);  //?????? ????????? result??? ?????? ???????????? ????????? ???????????? - status ???

        assertThat(result.getJob_id().equals(assignedResult.getJob_id())); //jobId ??????
        assertThat(result.getStatus().equals(TwtResponse_Base.StatusType.Ok)); //status ??????
    }
    @Test
        /*
         * getResponseFromDB Test1. Get TwtResponse_Tsptw Result Get Test
         */
    void getResponseFromDB2() throws IOException, ExecutionException, InterruptedException {
        //given
        ClassPathResource inputResource = new ClassPathResource("/json/TwtResponse_Tsptw_result.json");
        InputStream inputStream = inputResource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))  ;
        String inputJson = reader.lines().collect(Collectors.joining("\n"));

        ObjectMapper mapper = new ObjectMapper();
        TwtResponse_Tsptw twtResponseTsptw = null;
        try {
            twtResponseTsptw = mapper.readValue(inputJson, TwtResponse_Tsptw.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        assertNotNull(twtResponseTsptw);
        asyncTask.SetResponseToDB(twtResponseTsptw, Constants.Msg_Process_Done);

        //when
        TwtResponse_Base result =  asyncTask.GetResponseFromDB(twtResponseTsptw.getJob_id()).get();

        //then
        assertNotNull(result);  //?????? ????????? result??? ?????? ???????????? ????????? ???????????? - status ???
        assertThat(twtResponseTsptw).isEqualTo(result); //???????????? ????????? ???????????? ??????????????????.
    }
    @Test
    void procTwtTask() throws IOException, ExecutionException, InterruptedException {
        //given
        ClassPathResource inputResource = new ClassPathResource("/json/vv04_simple_no_tw.json");
        InputStream inputStream = inputResource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))  ;
        String inputJson = reader.lines().collect(Collectors.joining("\n"));

        TwtJobDesc jobDesc = TwtJobDesc.create(inputJson);

        TwtResponse_forAssignJob assignResult = asyncTask.assignJob(jobDesc.requestParam).get();

        //when
        TwtResponse_Tsptw twtResponseTsptw = asyncTask.procTwtTask(jobDesc).get();

        //then
        assertNotNull(twtResponseTsptw);
        ArrayList<TwtResponse_RouteActivity> activites = twtResponseTsptw.getSolution().getRoutes().get(0).getActivities();

        assertThat( activites.get(0).getLoc_name()).isEqualToIgnoringCase("mappers");
        assertThat( activites.get(1).getLoc_name()).isEqualToIgnoringCase("spo-any");
        assertThat( activites.get(2).getLoc_name()).isEqualToIgnoringCase("?????????");
        assertThat( activites.get(3).getLoc_name()).isEqualToIgnoringCase("?????????");
        assertThat( activites.get(4).getLoc_name()).isEqualToIgnoringCase("???????????????");
        assertThat( activites.get(5).getLoc_name()).isEqualToIgnoringCase("mappers");
        assertThat(twtResponseTsptw.getJob_id().equals(assignResult.getJob_id()));
    }

    @Test
    void procRequestTask() throws IOException, ExecutionException, InterruptedException {
        //given
        ClassPathResource inputResource = new ClassPathResource("/json/vv04_simple_no_tw.json");
        InputStream inputStream = inputResource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))  ;
        String inputJson = reader.lines().collect(Collectors.joining("\n"));

        TwtJobDesc jobDesc = TwtJobDesc.create(inputJson);
        //when
        TwtResponse_Tsptw twtResponseTsptw = asyncTask.procRequestTask(jobDesc);

        //then
        assertNotNull(twtResponseTsptw);
        ArrayList<TwtResponse_RouteActivity> activites = twtResponseTsptw.getSolution().getRoutes().get(0).getActivities();

        assertThat( activites.get(0).getLoc_name()).isEqualToIgnoringCase("mappers");
        assertThat( activites.get(1).getLoc_name()).isEqualToIgnoringCase("spo-any");
        assertThat( activites.get(2).getLoc_name()).isEqualToIgnoringCase("?????????");
        assertThat( activites.get(3).getLoc_name()).isEqualToIgnoringCase("?????????");
        assertThat( activites.get(4).getLoc_name()).isEqualToIgnoringCase("???????????????");
        assertThat( activites.get(5).getLoc_name()).isEqualToIgnoringCase("mappers");

    }
}
