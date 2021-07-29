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

        //when - ing로 assinged Result를 저장햇을 때
        asyncTask.SetResponseToDB((TwtResponse_forAssignJob) assignedResult, Constants.Msg_Process_Ing);

        //then - 저장한 jobId로 가져오면 해당 데이터가 정상적으로 담겨져 있어야함
        Optional<TwtResponseWrapper> wrapperResult = twtRepository.getResponseFromDB(assignedResult.getJob_id());

        assertThat(wrapperResult.isPresent()); //store 상에 해당 데이터가 존재해야되며

        TwtResponse_Tsptw result = (TwtResponse_Tsptw) wrapperResult.get().getResponse();

        assertNotNull(result);  //실제 저장된 result에 대해 정상적인 값들을 갖여아햠 - status 등

        assertThat(result.getJob_id().equals(assignedResult.getJob_id())); //jobId 체크
        assertThat(result.getStatus().equals(TwtResponse_Base.StatusType.Ok)); //status 체크
        assertThat(wrapperResult.get().getProcessStatus().equals(Constants.Msg_Process_Ing)); //프로세스 상태는 ing여야 함
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

        //when - ing로 assinged Result를 저장햇을 때
        asyncTask.SetResponseToDB((TwtResponse_Tsptw)twtResponseTsptw, Constants.Msg_Process_Done);
        Thread.sleep(1000); //SetResponseToDB는 비동기 함수이므로 삽입처리까지 대기시간을 줌

        //then - 저장한 jobId로 가져오면 해당 데이터가 정상적으로 담겨져 있어야함
        TwtResponse_Tsptw result = asyncTask.GetResponseFromDB(twtResponseTsptw.getJob_id()).get(); //request로 사용한 job id  로 db에서 조회

        assertNotNull(result);  //실제 저장된 result에 대해 정상적인 값들을 갖여아햠 - status 등
        assertThat(twtResponseTsptw).isEqualTo(result); //입력으로 들어간 데이터와 동일해야한다.
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
        assertNotNull(result);  //실제 저장된 result에 대해 정상적인 값들을 갖여아햠 - status 등

        assertThat(result.getJob_id().equals(assignedResult.getJob_id())); //jobId 체크
        assertThat(result.getStatus().equals(TwtResponse_Base.StatusType.Ok)); //status 체크
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
        assertNotNull(result);  //실제 저장된 result에 대해 정상적인 값들을 갖여아햠 - status 등
        assertThat(twtResponseTsptw).isEqualTo(result); //입력으로 들어간 데이터와 동일해야한다.
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
        assertThat( activites.get(2).getLoc_name()).isEqualToIgnoringCase("송파역");
        assertThat( activites.get(3).getLoc_name()).isEqualToIgnoringCase("석촌역");
        assertThat( activites.get(4).getLoc_name()).isEqualToIgnoringCase("송파나루역");
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
        assertThat( activites.get(2).getLoc_name()).isEqualToIgnoringCase("송파역");
        assertThat( activites.get(3).getLoc_name()).isEqualToIgnoringCase("석촌역");
        assertThat( activites.get(4).getLoc_name()).isEqualToIgnoringCase("송파나루역");
        assertThat( activites.get(5).getLoc_name()).isEqualToIgnoringCase("mappers");

    }
}
