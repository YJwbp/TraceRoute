package com.wbp.traceroute3;

import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by wbp on 2017/7/20.
 */

public class PingInfo {
    private String ip;
    private String time;
    private String geo;
    private String host;

    private String firstLine;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getFirstLine() {
        return firstLine;
    }

    public void setFirstLine(String firstLine) {
        this.firstLine = firstLine;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    @Override
    public String toString() {
        return "PingInfo{" +
                "ip='" + ip + '\'' +
                ", time='" + time + '\'' +
                ", geo='" + geo + '\'' +
                '}';
    }

    public void loadGeoByIP() {
        if (TextUtils.isEmpty(ip)) {
            this.geo = "";
            return;
        }
        try {
            Document document = Jsoup.connect("http://ip.cn/index.php").data("ip", ip).get();
            String location = document.getElementsByTag("code").last().text();// 北京市 联通
            this.geo = location;
        } catch (IOException e) {
            e.printStackTrace();
            this.geo = "";
        }

    }
}
