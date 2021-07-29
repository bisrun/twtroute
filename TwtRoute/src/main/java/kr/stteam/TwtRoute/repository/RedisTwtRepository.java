package kr.stteam.TwtRoute.repository;

import kr.stteam.TwtRoute.protocol.TwtResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RedisTwtRepository implements TwtRepository {

    private static Map<String, TwtResponseWrapper> store = new HashMap<>();

    private static Logger logger = LoggerFactory.getLogger(MemoryTwtRepository.class);

    @Override
    public void setResponseToDB(TwtResponseWrapper result) {

    }

    @Override
    public Optional<TwtResponseWrapper> getResponseFromDB(String job_id) {
        return Optional.empty();
    }

    @Override
    public void printDB() {

    }

    @Override
    public void clearStore() {

    }
}
