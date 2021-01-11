package com.swenkalski.blackchamber.helper;
/* 
    skalski created on 11/01/2021 inside the package - com.swenkalski.blackchamber.helper 
    Twitter: @KalskiSwen    
*/

import java.util.regex.Pattern;

public class Sanitization {

    public static boolean isHexSHA256(String pattern){
        return Pattern.compile("\\b[A-Fa-f0-9]{64}\\b").matcher(pattern).matches();
    }

    public static boolean isHexHalfedSHA256(String pattern){
        return Pattern.compile("\\b[A-Fa-f0-9]{32}\\b").matcher(pattern).matches();
    }

    public static boolean isSMailAddress(String pattern){
        String[] testPatters = pattern.split("//:");
        if(testPatters.length != 2){
            return false;
        }
        return (isDomain(testPatters[0]) && isUser(testPatters[0]));
    }

    public static boolean isDomain(String pattern){
        return Pattern.compile("^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$").matcher(pattern).matches();
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
    public static boolean isUser(String pattern){
        return Pattern.compile("^(?=[a-zA-Z0-9._]{8,20}$)(?!.*[_.]{2})[^_.].*[^_.]$").matcher(pattern).matches();
    }
}