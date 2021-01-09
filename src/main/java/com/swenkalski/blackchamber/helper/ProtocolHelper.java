package com.swenkalski.blackchamber.helper;

import java.util.regex.Pattern;

public class ProtocolHelper {

    public static final String LOCALHOST = "localhost";
    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";

    private static final String zeroTo255
            = "([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])";

    private static final String IP_REGEXP
            = zeroTo255 + "\\." + zeroTo255 + "\\."
            + zeroTo255 + "\\." + zeroTo255;

    private static final Pattern IP_PATTERN
            = Pattern.compile(IP_REGEXP);

    public static String getProtocol(String host) {
        if (host.equals(LOCALHOST) || isValid(host)) {
            return HTTP;
        }

        return HTTPS;
    }

    private static boolean isValid(String address) {
        return IP_PATTERN.matcher(address).matches();
    }

}
