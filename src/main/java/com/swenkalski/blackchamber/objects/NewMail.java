package com.swenkalski.blackchamber.objects;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;

@Getter
@Setter
public class NewMail {
    private String recipient;
    private String sender;
    private String mailId;
    private Long time;
    private Address senderAddress;
    private Address recipientAddress;

    public NewMail(String recipient, String sender, String mailId, Long time) {
        this.recipient = recipient;
        this.sender = sender;
        this.mailId = mailId;
        this.time = time;
        this.senderAddress = new Address(sender);
        this.recipientAddress = new Address(recipient);
    }

    public String getMailHash() {
        return DigestUtils.md5Hex(mailId+time).toUpperCase();
    }


}
