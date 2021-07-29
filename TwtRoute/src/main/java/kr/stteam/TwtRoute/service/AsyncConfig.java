package kr.stteam.TwtRoute.service;
import java.util.concurrent.Executor;

import javax.annotation.Resource;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    /** 기본 Thread 수 */
    private static int TASK_CORE_POOL_SIZE = 40;
    /** 최대 Thread 수 */
    private static int TASK_MAX_POOL_SIZE = 40;
    /** QUEUE 수 */
    private static int TASK_QUEUE_CAPACITY = 10000;        //요청작업이 저장되는 큐의 크기. 모든 스레드가 동작중이면 큐에서 대기됨
    /** Thread Bean Name */
    private static String EXECUTOR_BEAN_NAME = "twtRoute_executor";
    /** Thread 대기 시 timeOut 시간 */
    private static int TASK_KEEP_ALIVE_TIME = 600; //단위 초,
    /** Thread */
    @Resource(name = "twtRoute_executor")
    private ThreadPoolTaskExecutor twtRoute_executor;


    /**
     * Thread 생성
     */
    @Bean(name = "twtRoute_executor")
    @Override
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(TASK_CORE_POOL_SIZE);
        executor.setMaxPoolSize(TASK_MAX_POOL_SIZE);
        executor.setQueueCapacity(TASK_QUEUE_CAPACITY);
        executor.setBeanName(EXECUTOR_BEAN_NAME);
        executor.setKeepAliveSeconds(TASK_KEEP_ALIVE_TIME);
        executor.initialize();
        return executor;
    }

    /**
     * Thread 등록 가능 여부
     */
    public boolean isTaskExecute() {
        boolean rtn = true;

        System.out.println("TwtRoute_EXECUTOR.getActiveCount() : " + twtRoute_executor.getActiveCount());

        // 실행중인 task 개수가 최대 개수(max + queue)보다 크거나 같으면 false
        if (twtRoute_executor.getActiveCount() >= (TASK_MAX_POOL_SIZE + TASK_QUEUE_CAPACITY)) {
            rtn = false;
        }

        return rtn;
    }

    /**
     * Thread 등록 가능 여부
     */
    public boolean isTaskExecute(int createCnt) {
        boolean rtn = true;

        // 실행중인 task 개수 + 실행할 개수가 최대 개수(max + queue)보다 크거나 같으면 false
        if ((twtRoute_executor.getActiveCount() + createCnt) > (TASK_MAX_POOL_SIZE + TASK_QUEUE_CAPACITY)) {
            rtn = false;
        }

        return rtn;
    }

    /* (non-Javadoc)
     * @see org.springframework.scheduling.annotation.AsyncConfigurer#getAsyncUncaughtExceptionHandler()
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }
}
