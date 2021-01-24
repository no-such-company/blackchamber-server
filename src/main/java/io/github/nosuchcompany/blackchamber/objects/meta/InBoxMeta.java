package io.github.nosuchcompany.blackchamber.objects.meta;
/* 
    skalski created on 20/01/2021 inside the package - com.swenkalski.blackchamber.objects.meta 
    Twitter: @KalskiSwen    
*/

import io.github.nosuchcompany.blackchamber.objects.mailobjects.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InBoxMeta {
    private Long timecode;
    private Address sender;
    private String mailId;
}
