package kr.stteam.TwtRoute.service;

import kr.stteam.TwtRoute.controller.TwtResult;
import kr.stteam.TwtRoute.protocol.OsrmTripMatrixResponseParam;

public interface RouteProc {
    String requestTripMatrix(StringBuffer viaInfo);
    boolean setTripMatrixInResult(String responseJson, TwtResult TwtResult);

    String requestRouteGeometry(TwtResult twtResult);
    boolean setRouteGeometryInResult(String responseJson,TwtResult twtResult);
}
