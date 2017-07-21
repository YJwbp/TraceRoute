package com.wbp.traceroute3.event;

/**
 * Created by wbp on 2017/7/21.
 */

public class TraceInfoEvent {
    private String header;

    public TraceInfoEvent(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
