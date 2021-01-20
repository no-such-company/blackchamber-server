package com.swenkalski.blackchamber.objects.meta;
/* 
    skalski created on 20/01/2021 inside the package - com.swenkalski.blackchamber.objects.meta 
    Twitter: @KalskiSwen    
*/

import com.swenkalski.blackchamber.objects.mailobjects.Address;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InBoxMeta {
    private String timecode;
    private Address sender;
    private String mailId;
}
