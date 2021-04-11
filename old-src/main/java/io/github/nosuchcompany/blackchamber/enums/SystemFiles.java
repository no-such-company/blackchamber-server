package io.github.nosuchcompany.blackchamber.enums;

public enum SystemFiles {
    MSG("msg"),
    FMSG("fmsg"),
    META("meta");

    public final String fileName;

    private SystemFiles(String fileName) {
        this.fileName = fileName;
    }
}
