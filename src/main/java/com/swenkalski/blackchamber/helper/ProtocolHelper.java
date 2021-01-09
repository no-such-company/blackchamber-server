package com.swenkalski.blackchamber.helper;

public class ProtocolHelper {

    public static final String LOCALHOST = "localhost";
    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";

    public static String getProtocol(String host){
        if(host.equals(LOCALHOST)){
            return HTTP;
        }

        return HTTPS;
    }

}
