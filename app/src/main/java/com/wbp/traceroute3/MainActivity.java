package com.wbp.traceroute3;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    // 输入网址框
    private EditText et;
    private TextView textView;

    // 开始traceroute的button
    private Button searchButton;

    // 最大的ttl跳转 可以自己设定
    private final int MAX_TTL = 30;

    // 都是一些字符串 用于parse 用的
    private static final String PING = "PING";
    private static final String FROM_PING = "From";
    private static final String OTHER_FROM_PING = "rom";
    private static final String SMALL_FROM_PING = "from";
    private static final String PARENTHESE_OPEN_PING = "(";
    private static final String PARENTHESE_CLOSE_PING = ")";
    private static final String TIME_PING = "time=";
    private static final String EXCEED_PING = "exceed";
    private static final String UNREACHABLE_PING = "100%";

    // 初始化默认ttl 为1
    private int ttl = 1;
    private String ipToPing;
    // ping耗时
    private float elapsedTime;

    // 存放结果集的tarces
    private List<TracerouteContainer> traces = new ArrayList<>();
    ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et = (EditText) this.findViewById(R.id.domainName);
        textView = (TextView) this.findViewById(R.id.text);
        searchButton = (Button) this.findViewById(R.id.btn);
        searchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
//                try {
//                    java.lang.Process p = Runtime.getRuntime().exec("ping 61.135.169.125");
////                    java.lang.Process p = Runtime.getRuntime().exec("ping www.baidu.com");
//                    p.waitFor();
//                    int len;
//                    if ((len = p.getErrorStream().available()) > 0) {
//                        byte[] buf = new byte[len];
//                        p.getErrorStream().read(buf);
//                        System.err.println("Command error:\t\""+new String(buf)+"\"");
//                    }
//                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(
//                            p.getInputStream()));
//                    String content = stdInput.readLine();
//
//                    Toast.makeText(v.getContext(), content, Toast.LENGTH_SHORT).show();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                final String TAG ="rxjava";
                Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            Document document = Jsoup.connect("http://ip.cn/index.php").data("ip", "202.106.48.9").get();
                            String title = document.title();
                            String location = document.getElementsByTag("code").last().text();// 北京市 联通
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Object>() {
                            @Override
                            public void onCompleted() {
                                Log.d(TAG, "onCompleted: ");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "onError: "+e);
                            }

                            @Override
                            public void onNext(Object o) {
                                Log.d(TAG, "onNext: "+o);
                            }
                        });


//                new ExecuteTracerouteAsyncTask(MAX_TTL, et.getText().toString())
//                        .execute();
            }
        });
    }

    private void showResultInLog() {
        for (TracerouteContainer container : traces) {
            Log.v("ccc", container.toString());
        }
    }

    /**
     * 这个任务就是来更新我们的后台log 日志 把所得到的traceroute信息打印出来。
     */
    private class ExecuteTracerouteAsyncTask extends
            AsyncTask<Void, Void, String> {

        private int maxTtl;

        private String url;

        public ExecuteTracerouteAsyncTask(int maxTtl, String url) {
            this.maxTtl = maxTtl;
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            String res = "";
            try {
                res = launchPing(url);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            TracerouteContainer trace;

            if (res.contains(UNREACHABLE_PING) && !res.contains(EXCEED_PING)) {
                trace = new TracerouteContainer("", parseIpFromPing(res),
                        elapsedTime);
            } else {
                trace = new TracerouteContainer("", parseIpFromPing(res),
                        ttl == maxTtl ? Float
                                .parseFloat(parseTimeFromPing(res))
                                : elapsedTime);
            }

            trace.setPingContent(res);
            InetAddress inetAddr;
            try {
                inetAddr = InetAddress.getByName(trace.getIp());
                String hostname = inetAddr.getHostName();
                trace.setHostName(hostname);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            traces.add(trace);
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            // 如果为空的话就截止吧 过程完毕
            if (TextUtils.isEmpty(result)) {
                Toast.makeText(getApplicationContext(), "Completed", Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder stringBuilder = new StringBuilder();
            for (TracerouteContainer trace : traces) {
                stringBuilder.append(trace.getPingContent()).append("\n\n");
            }
            textView.setText(stringBuilder.toString());
            // 如果这一跳的ip地址与最终的地址 一致的话 就说明 ping到了终点
            if (traces.get(traces.size() - 1).getIp().equals(ipToPing)) {
                Toast.makeText(getApplicationContext(), "Completed", Toast.LENGTH_SHORT).show();
                if (ttl < maxTtl) {
                    ttl = maxTtl;
                    traces.remove(traces.size() - 1);
                    new ExecuteTracerouteAsyncTask(maxTtl, url).execute();
                } else {
                    // 如果ttl ==maxTtl的话 当然就结束了 我们就要打印出最终的结果
                    showResultInLog();
                }
            } else {
                // 如果比较的ip 不相等 哪就说明还没有ping到最后一跳。我们就需要继续ping
                // 继续ping的时候 记得ttl的值要加1
                if (ttl < maxTtl) {
                    ttl++;
                    new ExecuteTracerouteAsyncTask(maxTtl, url).execute();
                }
            }
            super.onPostExecute(result);
        }

        private String launchPing(String url) throws IOException, InterruptedException {
            java.lang.Process p;
            String command = "";

            // 这个实际上就是我们的命令第一封装 注意ttl的值的变化 第一次调用的时候 ttl的值为1
            String format = "ping -c 3 -t 1000 -m %d ";
            command = String.format(format, ttl);

            long startTime = System.nanoTime();
            // 实际调用命令时 后面要跟上url地址
            p = Runtime.getRuntime().exec(command + url);
            int len;
            if ((len = p.getErrorStream().available()) > 0) {
                byte[] buf = new byte[len];
                p.getErrorStream().read(buf);
                System.err.println("Command error ttl:\t\"" + ttl + new String(buf) + "\"");
            }
            p.waitFor();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));

            String s;
            String res = "";
            while ((s = stdInput.readLine()) != null) {
                res += s + "\n";
                // 这个地方这么做的原因是 有的手机 返回的from 有的手机返回的是From所以要
                // 这么去判定 请求结束的事件 算一下 延时
                if (s.contains(FROM_PING) || s.contains(SMALL_FROM_PING) || s.contains(OTHER_FROM_PING)) {
                    elapsedTime = (System.nanoTime() - startTime) / 1000000.0f;
                }
            }

            // 调用结束的时候 销毁这个资源
            p.destroy();

            if (res.equals("")) {
                throw new IllegalArgumentException();
            }
            // 第一次调用ping命令的时候 记得把取得的最终的ip地址 赋给外面的ipToPing
            // 后面要依据这个ipToPing的值来判断是否到达ip数据报的 终点
            if (ttl == 1) {
                ipToPing = parseIpToPingFromPing(res);
            }
            return res;
        }

        /**
         * 从结果集中解析出ip
         *
         * @param ping
         * @return
         */
        private String parseIpFromPing(String ping) {
            String ip = "";
            if (ping.contains(FROM_PING)) {
                int index = ping.indexOf(FROM_PING);

                ip = ping.substring(index + 5);
                if (ip.contains(PARENTHESE_OPEN_PING)) {
                    int indexOpen = ip.indexOf(PARENTHESE_OPEN_PING);
                    int indexClose = ip.indexOf(PARENTHESE_CLOSE_PING);

                    ip = ip.substring(indexOpen + 1, indexClose);
                } else {
                    ip = ip.substring(0, ip.indexOf("\n"));
                    if (ip.contains(":")) {
                        index = ip.indexOf(":");
                    } else {
                        index = ip.indexOf(" ");
                    }

                    ip = ip.substring(0, index);
                }
            } else {
                int indexOpen = ping.indexOf(PARENTHESE_OPEN_PING);
                int indexClose = ping.indexOf(PARENTHESE_CLOSE_PING);

                ip = ping.substring(indexOpen + 1, indexClose);
            }

            return ip;
        }


        /**
         * 从结果集中解析出ip
         *
         * @param ping
         * @return
         */
        private String parseIpToPingFromPing(String ping) {
            String ip = "";
            if (ping.contains(PING)) {
                int indexOpen = ping.indexOf(PARENTHESE_OPEN_PING);
                int indexClose = ping.indexOf(PARENTHESE_CLOSE_PING);

                ip = ping.substring(indexOpen + 1, indexClose);
            }

            return ip;
        }

        /**
         * 从结果集中解析出time
         *
         * @param ping
         * @return
         */
        private String parseTimeFromPing(String ping) {
            String time = "";
            if (ping.contains(TIME_PING)) {
                int index = ping.indexOf(TIME_PING);

                time = ping.substring(index + 5);
                index = time.indexOf(" ");
                time = time.substring(0, index);
            }

            return time;
        }
    }
}
