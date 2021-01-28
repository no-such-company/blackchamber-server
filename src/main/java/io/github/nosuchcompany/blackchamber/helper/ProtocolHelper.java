package io.github.nosuchcompany.blackchamber.helper;

import static io.github.nosuchcompany.blackchamber.helper.Sanitization.isIp4Address;

public class ProtocolHelper {

    public static final String LOCALHOST = "localhost";
    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";

    public static String getProtocol(String host) {
        if (host.equals(LOCALHOST) || isIp4Address(host)) {
            return HTTP;
        }

        return HTTPS;
    }
}
