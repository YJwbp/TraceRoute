package com.wbp.traceroute3;

import java.util.List;

/**
 * Created by wbp on 2017/7/20.
 */

public class TTLInfo {
    private int ttl;
    private List<PingInfo> pingInfoList;

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public List<PingInfo> getPingInfoList() {
        return pingInfoList;
    }

    public void setPingInfoList(List<PingInfo> pingInfoList) {
        this.pingInfoList = pingInfoList;
    }
}
