package com.wbp.traceroute3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    /**
     * 输入网址框
     */
    private EditText etDomainName;
    /**
     * 显示结果
     */
    private TextView tvTraces;

    private Button btnStart;
    private Button btnStop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etDomainName = (EditText) this.findViewById(R.id.et_domainName);
        tvTraces = (TextView) this.findViewById(R.id.text);
        btnStart = (Button) this.findViewById(R.id.btn_start);
        btnStop = (Button) this.findViewById(R.id.btn_stop);

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


}
