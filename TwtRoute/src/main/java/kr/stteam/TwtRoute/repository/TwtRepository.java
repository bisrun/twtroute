package kr.stteam.TwtRoute.repository;

import kr.stteam.TwtRoute.protocol.TwtResponseWrapper;

import java.util.Optional;

public interface TwtRepository {
    void setResponseToDB(TwtResponseWrapper result);
    Optional<TwtResponseWrapper> getResponseFromDB(String job_id);
    void printDB();
    void clearStore();
}
