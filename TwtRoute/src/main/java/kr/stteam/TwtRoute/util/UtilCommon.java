package kr.stteam.TwtRoute.util;

public class UtilCommon {
    public static double convHMtoSec(String sHourMin) {
        String array[] = sHourMin.split(":");
        Double secHour = Double.parseDouble(array[0]) * 3600 ;
        Double secMin = Double.parseDouble(array[1]) * 60 ;
        return secHour+secMin;
    }
    public static  String convSectoHM(double sec) {
        String HM = String.format("%02d:%02d", (int)(sec/3600), (int)((sec%3600)/60));
        return HM;
    }

    public static  String convSectoHMS(double sec) {
        String HMS = String.format("%02d:%02d:%02d", (int)(sec/3600), (int)((sec%3600)/60),(int)(sec%60)) ;
        return HMS;
    }
}
