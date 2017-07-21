package com.wbp.traceroute3.event;

import com.wbp.traceroute3.TTLInfo;

/**
 * Created by wbp on 2017/7/21.
 */

public class TTLInfoEvent {
    private TTLInfo info;

    public TTLInfoEvent(TTLInfo info) {
        this.info = info;
    }

    public TTLInfo getInfo() {
        return info;
    }

    public void setInfo(TTLInfo info) {
        this.info = info;
    }
}
