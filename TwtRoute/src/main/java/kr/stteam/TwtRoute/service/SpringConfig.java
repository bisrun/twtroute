package kr.stteam.TwtRoute.service;

import kr.stteam.TwtRoute.repository.MemoryTwtRepository;
import kr.stteam.TwtRoute.repository.TwtRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {
//    @Bean
//    public TwtService twtService(){
//        return new TwtService(appProperties(), routeProcOSRM());
//    }
//    @Bean
//    public AppProperties appProperties() {
//        return new AppProperties();
//    }
//
//    @Bean
//    public RouteProcOSRM routeProcOSRM(){
//        return new RouteProcOSRM(appProperties());
//    }

    @Bean
    public TwtRepository twtRepository(){
        //return new RedisTwtRepository(); //추후  redis로 변경시, 여기서만 twtRepo를 변경해준다.
        return new MemoryTwtRepository();
    }

}
