package com.wbp.traceroute3;

/**
 * Created by wbp on 2017/7/19.
 */

public class TracerouteContainer {
    private String ip;
    private String hostName;
    private float ttl;
    private String pingContent;

    public TracerouteContainer(String hostName, String ip, float ttl) {
        this.ip = ip;
        this.hostName = hostName;
        this.ttl = ttl;
    }

    public String getPingContent() {
        return pingContent;
    }

    public void setPingContent(String pingContent) {
        this.pingContent = pingContent;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public float getTtl() {
        return ttl;
    }

    public void setTtl(float ttl) {
        this.ttl = ttl;
    }

    @Override
    public String toString() {
        return "TracerouteContainer{" +
                "ip='" + ip + '\'' +
                ", hostName='" + hostName + '\'' +
                ", ttl=" + ttl +
                '}';
    }
}
