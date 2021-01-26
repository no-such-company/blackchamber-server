package io.github.nosuchcompany.blackchamber.objects.mailobjects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewMail {
    private String recipient;
    private String sender;
    private String mailId;
    private Long time;
    private Address senderAddress;
    private Address recipientAddress;

    public NewMail(String recipient, String sender, String mailId, Long time) throws Exception {
        this.recipient = recipient;
        this.sender = sender;
        this.mailId = mailId;
        this.time = time;
        this.senderAddress = new Address(sender);
        this.recipientAddress = new Address(recipient);
    }
}
