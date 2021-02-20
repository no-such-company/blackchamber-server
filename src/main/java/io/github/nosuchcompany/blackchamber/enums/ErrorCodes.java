package io.github.nosuchcompany.blackchamber.enums;

public enum ErrorCodes {
    // LVL 0
    FINE( "E00", "Operation was successful - goodbye"),

    // LVL 1 Error Codes "Userspace"
    MAX_QUOTA_ERR("E11", "Userspace is full"),
    NO_SUCH_USER("E12", "User not found"),
    BAD_CREDENTIALS("E13", "Credentials mismatch"),

    // LVL 2 Error Codes "Send/Receive Mails Issues"
    PRP_ISSUE("E21", "Probing of files failed"),
    SENDER_BLACKLISTED("E22", "The Sender is Blacklisted"),

    // LVL 3 Error Codes "System Errors"
    META("E31", "Metadata creation failed"),
    CPY("E32", "Moving of files failed"),
    KEY_CREATION_EXCEPTION("E33", "Creation of keys failed"),
    USER_CREATION_EXEPTION("E34", "Can't create user"),
    USER_CREATION_CREDENTIAL_MISMATCH("E34", "Can't create user, because no public registration allowed"),
    SERVER_ERROR("E39", "Unsolved Server Error");

    public final String errorcode;
    public final String description;

    private ErrorCodes(String errorcode, String description) {
        this.errorcode = errorcode;
        this.description = description;
    }
}
