package com.swenkalski.blackchamber.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;

@Getter
@Setter
@AllArgsConstructor
public class NewMail {
    private String recipient;
    private String sender;
    private String mailId;
    private Long time;

    public String getMailHash() {
        return DigestUtils.md5Hex(mailId+time).toUpperCase();
    }
}
