package kr.stteam.TwtRoute.service;

import kr.stteam.TwtRoute.domain.TwtJobDesc;

public interface RouteProc {
    String requestTripMatrix(StringBuffer viaInfo);
    boolean setTripMatrixInResult(String responseJson, TwtJobDesc TwtJobDesc);

    String requestRouteGeometry(TwtJobDesc twtJobDesc);
    boolean setRouteGeometryInResult(String responseJson, TwtJobDesc twtJobDesc);
}
