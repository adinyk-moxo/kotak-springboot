package com.kotak.onboarding.model;

public class VideoStatusRequest {
    private String binderId;
    private String status;

    public String getBinderId() { return binderId; }
    public String getStatus()   { return status; }

    public void setBinderId(String v) { binderId = v; }
    public void setStatus(String v)   { status = v; }
}
