package com.wbp.traceroute3;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wbp.traceroute3.adapter.TTLAdapter;
import com.wbp.traceroute3.event.TTLInfoEvent;
import com.wbp.traceroute3.event.TraceCompleteEvent;
import com.wbp.traceroute3.event.TraceInfoEvent;

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
    @BindView(R.id.progress)
    ProgressBar progressBar;
    @BindView(R.id.tv_trace_info)
    TextView tvTraceInfo;
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
                startTrace();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTrace();
            }
        });
    }


    private void startTrace() {
        progressBar.setVisibility(View.VISIBLE);
        ttlAdapter.getDatas().clear();
        ttlAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Start!", Toast.LENGTH_SHORT).show();
        TraceHandler.INSTANCE.url(etDomainName.getText().toString()).startTrace();
    }

    private void stopTrace() {
        TraceHandler.INSTANCE.stopTrace();
    }

    private void initViews() {
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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveTraceCompleteEvent(TraceCompleteEvent event) {
        Toast.makeText(this, "Completed!", Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveTraceInfoEvent(TraceInfoEvent event) {
        tvTraceInfo.setText(event.getHeader());
    }
}
