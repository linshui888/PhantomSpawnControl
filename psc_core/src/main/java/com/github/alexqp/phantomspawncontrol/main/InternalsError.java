package com.github.alexqp.phantomspawncontrol.main;

public class InternalsError extends Exception {

    private String version = "no version specified";

    private InternalsError(String msg) {
        super(msg);
    }

    public InternalsError(String msg, String version) {
        this(msg);
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
