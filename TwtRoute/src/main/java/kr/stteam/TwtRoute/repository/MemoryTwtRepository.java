package kr.stteam.TwtRoute.repository;

import kr.stteam.TwtRoute.protocol.TwtResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class MemoryTwtRepository implements TwtRepository {

    private static Map<String, TwtResponseWrapper> store = new HashMap<>();

    private static Logger logger = LoggerFactory.getLogger(MemoryTwtRepository.class);

    @Override
    public void setResponseToDB(TwtResponseWrapper result) {
        //db에 result 값을 기록한다.
        //status 값이 Done일 경우에만 결과값이 의미를 갖고, 그전까지는 처리중임에 의미를 갖는다.
        logger.info("[MemoryRepo][Insert] job-id: " + result.getResponse().getJob_id() + "  status : " + result.getProcessStatus());
        store.put(result.getResponse().getJob_id(), result);
    }

    @Override
    public Optional<TwtResponseWrapper> getResponseFromDB(String job_id) {
        //db에 result 값을 기록한다.
        //status 값이 Done일 경우에만 결과값이 의미를 갖고, 그전까지는 처리중임에 의미를 갖는다.

        logger.info("[MemoryRepo][Get] job-id: " + job_id);
        TwtResponseWrapper response_forDB = store.get(job_id);
        return Optional.ofNullable(response_forDB);
    }
    /*
     Test용 Print 함수
     */
    @Override
    public void printDB(){
        System.out.println("========== Print DB Start =============");
        System.out.println("Job_ID\t\t\t\t\t|Status\t|responseJson\t|");
        for (String key: store.keySet()){
            System.out.println(key + "\t" + store.get(key).getProcessStatus() + "\t" + store.get(key).getResponse());
        }
        System.out.println("========== Print DB End =============");
    }

    @Override
    public void clearStore() {
        store.clear();
    }
}
