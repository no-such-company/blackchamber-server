package io.github.nosuchcompany.blackchamber.helper;
/* 
    skalski created on 11/01/2021 inside the package - com.swenkalski.blackchamber.helper 
    Twitter: @KalskiSwen    
*/

import java.util.regex.Pattern;

public class Sanitization {

    private static final String zeroTo255
            = "([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])";

    private static final String IP_REGEXP
            = zeroTo255 + "\\." + zeroTo255 + "\\."
            + zeroTo255 + "\\." + zeroTo255;

    private static final Pattern IP_PATTERN
            = Pattern.compile(IP_REGEXP);

    public static boolean isHexSHA256(String pattern) {
        return Pattern.compile("\\b[A-Fa-f0-9]{64}\\b").matcher(pattern).matches();
    }

    public static boolean isHexHalfedSHA256(String pattern) {
        return Pattern.compile("\\b[A-Fa-f0-9]{31}\\b").matcher(pattern).matches();
    }

    public static boolean isSMailAddress(String pattern) {
        String[] testPatters = pattern.split("//:");
        if (testPatters.length != 2) {
            return false;
        }
        return (isDomain(testPatters[0]) && isUser(testPatters[1]));
    }

    public static boolean isDomain(String pattern) {
        if (pattern.equals("localhost")) {
            return true;
        }
        if (isIp4Address(pattern)) {
            return true;
        }
        return Pattern.compile("^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$").matcher(pattern).matches();
    }

    public static boolean isIp4Address(String pattern) {
        return IP_PATTERN.matcher(pattern).matches();
    }


    /*
        ^(?=.{8,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$
     └─────┬────┘└───┬──┘└─────┬─────┘└─────┬─────┘ └───┬───┘
           │         │         │            │           no _ or . at the end
           │         │         │            │
           │         │         │            allowed characters
           │         │         │
           │         │         no __ or _. or ._ or .. inside
           │         │
           │         no _ or . at the beginning
           │
           username is 8-20 characters long
     */
    public static boolean isUser(String pattern) {
        return Pattern.compile("^(?=[a-zA-Z0-9._]{8,20}$)(?!.*[_.]{2})[^_.].*[^_.]$").matcher(pattern).matches();
    }

    public static boolean isValidFolderNamePattern(String pattern) {
        return Pattern.compile("^[a-zA-Zа-яА-Я0-9_!]+$").matcher(pattern).matches();
    }
}
