package kr.stteam.TwtRoute.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.stteam.TwtRoute.protocol.TwtResponseWrapper;
import kr.stteam.TwtRoute.protocol.TwtResponse_Base;
import kr.stteam.TwtRoute.protocol.TwtResponse_Tsptw;
import kr.stteam.TwtRoute.protocol.TwtResponse_forAssignJob;
import kr.stteam.TwtRoute.util.Constants;
import kr.stteam.TwtRoute.util.UtilCommon;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MemoryTwtRepositoryTest {

    TwtRepository twtRepository;

    @BeforeEach
    public void beforeEach(){
        twtRepository = new MemoryTwtRepository();
        //twtService = new TwtService(appProperties, routeProcOSRM);
       // asyncTask = new AsyncTask(twtService, twtRepository);
    }

    @AfterEach()
    public void afterEach() {
        twtRepository.clearStore();
    }


    @Test
    /*
        Test1. assignedJob Set Test
     */
    void setResponseToDB1() {
        //given
        TwtResponse_forAssignJob assignedResult = new TwtResponse_forAssignJob();
        assignedResult.setJob_id(UtilCommon.defineJobId());
        assignedResult.setStatus(TwtResponse_Base.StatusType.Ok);
        assignedResult.setReg_job_time(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())); //현재시간으로 등록시간을 사용

        TwtResponseWrapper wrappedData = new TwtResponseWrapper(assignedResult, Constants.Msg_Process_Ing);

        //when
        twtRepository.setResponseToDB(wrappedData);

        //then
        Optional<TwtResponseWrapper> wrapperResult = twtRepository.getResponseFromDB(assignedResult.getJob_id());

        assertThat(wrapperResult.isPresent()); //store 상에 해당 데이터가 존재해야되며
        assertThat(wrappedData).isEqualTo(wrapperResult.get()); //입력으로 들어간 데이터와 동일해야한다.
    }
    @Test
    /*
        Test1. TwtResponse_Tsptw Set Test
     */
    void setResponseToDB2() throws IOException {
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

        TwtResponseWrapper wrappedData = new TwtResponseWrapper(twtResponseTsptw, Constants.Msg_Process_Done);

        //when - Done으로 twtResponseTsptw Result를 저장햇을 때
        twtRepository.setResponseToDB(wrappedData);

        //then - 저장한 jobId로 가져오면 해당 데이터가 정상적으로 담겨져 있어야함
        Optional<TwtResponseWrapper> wrapperResult = twtRepository.getResponseFromDB(twtResponseTsptw.getJob_id());

        assertThat(wrapperResult.isPresent()); //store 상에 해당 데이터가 존재해야되며
        assertThat(wrappedData).isEqualTo(wrapperResult.get()); //입력으로 들어간 데이터와 동일해야한다.
    }

    @Test
    /*
       Test1. assignedJob Get Test
     */
    void getResponseFromDB1() {
        //given
        TwtResponse_forAssignJob assignedResult = new TwtResponse_forAssignJob();
        assignedResult.setJob_id(UtilCommon.defineJobId());
        assignedResult.setStatus(TwtResponse_Base.StatusType.Ok);
        assignedResult.setReg_job_time(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())); //현재시간으로 등록시간을 사용

        TwtResponseWrapper wrappedData = new TwtResponseWrapper(assignedResult, Constants.Msg_Process_Ing);

        twtRepository.setResponseToDB(wrappedData);

        //when
        Optional<TwtResponseWrapper> wrapperResult = twtRepository.getResponseFromDB(assignedResult.getJob_id());

        //then

        assertThat(wrapperResult.isPresent()); //store 상에 해당 데이터가 존재해야되며
        assertThat(wrappedData).isEqualTo(wrapperResult.get()); //입력으로 들어간 데이터와 동일해야한다.
    }


    @Test
    /*
       Test1. TwtResponse_Tsptw Get Test
     */
    void getResponseFromDB2() throws IOException {
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

        TwtResponseWrapper wrappedData = new TwtResponseWrapper(twtResponseTsptw, Constants.Msg_Process_Done);
        twtRepository.setResponseToDB(wrappedData);

        //when - Done으로 twtResponseTsptw Result를 저장햇을 때
        Optional<TwtResponseWrapper> wrapperResult = twtRepository.getResponseFromDB(twtResponseTsptw.getJob_id());

        //then - 저장한 jobId로 가져오면 해당 데이터가 정상적으로 담겨져 있어야함
        assertThat(wrapperResult.isPresent()); //store 상에 해당 데이터가 존재해야되며
        assertThat(wrappedData).isEqualTo(wrapperResult.get()); //입력으로 들어간 데이터와 동일해야한다.
    }
}
