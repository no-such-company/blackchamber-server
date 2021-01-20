package com.swenkalski.blackchamber.objects.meta;
/* 
    skalski created on 20/01/2021 inside the package - com.swenkalski.blackchamber.objects.meta 
    Twitter: @KalskiSwen    
*/

import com.swenkalski.blackchamber.objects.mailobjects.Address;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OutBoxMeta {
    private Long timecode;
    private List<Address> recipients;
    private String mailId;

    public OutBoxMeta(Long timecode, List<String> recipientsAddresses, String mailId) {
        this.timecode = timecode;
        recipients = new ArrayList<>();
        recipientsAddresses.forEach(address -> {
            try {
                recipients.add(new Address(address));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        this.recipients = recipients;
        this.mailId = mailId;
    }
}
