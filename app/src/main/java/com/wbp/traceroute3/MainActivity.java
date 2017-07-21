package com.wbp.traceroute3;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wbp.traceroute3.adapter.TTLAdapter;
import com.wbp.traceroute3.event.TTLInfoEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.et_domainName)
    TextView etDomainName;
    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.btn_stop)
    Button btnStop;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private TTLAdapter ttlAdapter = new TTLAdapter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        initViews();
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TraceHandler.INSTANCE.url(etDomainName.getText().toString()).startTrace();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TraceHandler.INSTANCE.stopTrace();
            }
        });
    }


    private void initViews(){
        LinearLayoutManager glm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(glm);
        mRecyclerView.setAdapter(ttlAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        TraceHandler.INSTANCE.stopTrace();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveTTLEvent(TTLInfoEvent event) {
        TTLInfo ttlInfo = event.getInfo();
        ttlAdapter.addOneTail(ttlInfo);
//
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("\n" + ttlInfo.getTtl() + "\n");
//        for (PingInfo ping :
//                ttlInfo.getPingInfoList()) {
//            stringBuilder.append(ping.toString() + "\n");
//        }
//        stringBuilder.append("\n\n");
//        tvTraces.setText(tvTraces.getText() + stringBuilder.toString());
    }

}
