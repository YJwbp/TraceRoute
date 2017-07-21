package com.wbp.traceroute3;

import android.text.TextUtils;
import android.util.Log;

import com.wbp.traceroute3.event.TTLInfoEvent;
import com.wbp.traceroute3.event.TraceCompleteEvent;
import com.wbp.traceroute3.event.TraceInfoEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by wbp on 2017/7/20.
 */

public enum TraceHandler {
    INSTANCE;

    private static final String TAG = "TraceHandler";
    private String destIP = "";
    private String traceUrl = "www.baidu.com";
    private int pingRepeatTimes = 3;
    // 最大的ttl跳转 可以自己设定
    private static final int MAX_TTL = 30;

    protected CompositeSubscription compositeSubscription;
    // traceroute to www.a.shifen.com (61.135.169.125), 64 hops max, 52 byte packets

    public TraceHandler url(String url) {
        this.traceUrl = url;
        return this;
    }

    public void startTrace() {
        List<Integer> ttls = new ArrayList<>();
        for (int i = 0; i < MAX_TTL; i++) {
            ttls.add(i, i + 1);
        }

        addSubscription(Observable.from(ttls)
                .map(new Func1<Integer, TTLInfo>() {
                    @Override
                    public TTLInfo call(Integer integer) {
                        return doTTL(integer);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TTLInfo>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: ");
                    }

                    @Override
                    public void onNext(TTLInfo ttlInfo) {
                        Log.d(TAG, "onNext: " + ttlInfo);
                        EventBus.getDefault().post(new TTLInfoEvent(ttlInfo));
                        checkTraceOver(ttlInfo);
                    }
                }));
    }

    private void prepareInfo() {
        String res = ping("ping -c 1 " + traceUrl);
        String subRes = res.substring(0, res.indexOf("\n"));
        if (subRes.contains("PING")) {
            destIP = parseDestIPFromPingResult(subRes);
            EventBus.getDefault().post(new TraceInfoEvent(subRes.replace("PING", "TraceRoute")));
        }
    }

    private void checkTraceOver(TTLInfo info) {
        StringBuilder sb = new StringBuilder("Check");
        for (PingInfo pingInfo : info.getPingInfoList()) {
            sb.append(pingInfo.getIp());
        }

        if (!TextUtils.isEmpty(destIP) && sb.toString().contains(destIP)) {
            stopTrace();
        }
    }

    public void stopTrace() {
        if (compositeSubscription != null) {
            compositeSubscription.clear();
        }
        EventBus.getDefault().post(new TraceCompleteEvent());
    }

    private void addSubscription(Subscription subscription) {
        if (compositeSubscription == null) {
            compositeSubscription = new CompositeSubscription();
        }
        compositeSubscription.add(subscription);
    }

    /**
     * 执行一次ttl操作
     * 耗时任务
     *
     * @param ttl
     * @return
     */
    private TTLInfo doTTL(int ttl) {
        if (ttl == 1) {
            prepareInfo();
        }
        TTLInfo ttlInfo = new TTLInfo();
        ttlInfo.setTtl(ttl);

        List<PingInfo> infos = new ArrayList<>();
        for (int i = 0; i < pingRepeatTimes; i++) {
            infos.add(ping(ttl));
        }
        ttlInfo.setPingInfoList(infos);
        return ttlInfo;
    }

    /**
     * 一次ping操作
     * 耗时任务
     *
     * @param ttl
     */
    private PingInfo ping(int ttl) {
        PingInfo pingInfo = new PingInfo();

        // 1、执行ping命令,得到返回字符串
        // 这个实际上就是我们的命令第一封装 注意ttl的值的变化 第一次调用的时候 ttl的值为1
        // -c表示ping执行次数; -t 超时; -m ttl
        String format = "ping -c 1 -t %d ";
        String command = String.format(format, ttl);

        String pingResult = ping(command + traceUrl);

        String ip = parseNodeIPFromPingResult(pingResult);
        pingInfo.setIp(ip);
        pingInfo.setTime(parseTimeFromPingResult(ping("ping -c 1 " + ip)));
        pingInfo.loadGeoByIP();
        return pingInfo;
    }

    private String ping(String command) {
        try {
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();

            // 调试信息
            int len;
            if ((len = p.getErrorStream().available()) > 0) {
                byte[] buf = new byte[len];
                p.getErrorStream().read(buf);
                Utils.log("Command error " + new String(buf));
            }

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s;
            StringBuilder sbResult = new StringBuilder();
            while ((s = stdInput.readLine()) != null) {
                sbResult.append(s).append("\n");
            }

            // 调用结束的时候 销毁这个资源
            p.destroy();

            if (TextUtils.isEmpty(sbResult.toString())) {
                Log.d(TAG, "ping: sbResult == null");
            }
            return sbResult.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "";
        }
    }

    //  解析目的IP,判断跟踪结束
    // host获取——伪需求
    // TODO: 2017/7/21 时间获取
    // 开始信息

    /**
     * 从ping的结果中提取目的节点ip地址
     *
     * @param result
     * @return
     */
    private String parseDestIPFromPingResult(String result) {
        String regex = "\\b\\d+.\\d+.\\d+.\\d+\\b";
        Pattern p = Pattern.compile(regex);

        //2, 通过正则对象获取匹配器对象。
        Matcher matcher = p.matcher(result);
        if (matcher.find()) {
            return matcher.group();
        }

        // 2、解析返回结果: PING www.a.shifen.com (61.135.169.121): 56 data bytes
        // 2.1 得到节点IP: 36 bytes from 10.2.151.252: Time to live exceeded  >>> IP转地理位置 >> 显示
        // 2.2 超时: 只有 Request timeout for icmp_seq 5  >> 显示 ***
        // 2.3 访问到了终点: 64 bytes from 61.135.169.121: icmp_seq=0 ttl=56 time=2.230 ms  >> 结束
        return "";
    }

    /**
     * 从ping的结果中提取ttl节点ip地址
     *
     * @param result
     * @return
     */
    private String parseNodeIPFromPingResult(String result) {
        String subResult = result.substring(result.indexOf("\n"));
        String regex = "\\b\\d+.\\d+.\\d+.\\d+\\b";
        Pattern p = Pattern.compile(regex);

        //2, 通过正则对象获取匹配器对象。
        Matcher matcher = p.matcher(subResult);
        if (matcher.find()) {
            return matcher.group();
        }

        return "";
    }

    private String parseTimeFromPingResult(String result) {
        String regex = "\\b\\d+.\\d+\\s*ms\\b";
        Pattern p = Pattern.compile(regex);

        //2, 通过正则对象获取匹配器对象。
        Matcher matcher = p.matcher(result);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }
}
