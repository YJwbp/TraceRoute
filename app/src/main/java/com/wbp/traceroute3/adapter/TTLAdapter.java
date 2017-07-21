package com.wbp.traceroute3.adapter;

import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import com.wbp.traceroute3.PingInfo;
import com.wbp.traceroute3.R;
import com.wbp.traceroute3.TTLInfo;

import java.util.List;

import butterknife.BindView;

/**
 * Created by wbp on 2017/7/21.
 */

public class TTLAdapter extends CommonAdapter<TTLInfo> {
    @Override
    public void initHolders() {
        SparseArray<Class> array = new SparseArray<>();
        array.put(Holder.resId, Holder.class);
        holderFactory = new HolderFactory(array);
    }

    class Holder extends CommonHolder<List<TTLInfo>> {
        public static final int resId = R.layout.item_node;

        @BindView(R.id.tv_node)
        TextView tvNode;
        @BindView(R.id.tv_ip)
        TextView tvIP;
        @BindView(R.id.tv_host)
        TextView tvHost;
        @BindView(R.id.tv_geo)
        TextView tvGeo;
        @BindView(R.id.tv_time)
        TextView tvTime;

        /**
         * 新Holder 需要修改资源ID
         *
         * @param itemView
         */
        public Holder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(List<TTLInfo> datas, int position) {
            TTLInfo info = datas.get(position);
            PingInfo pingInfo = info.getPingInfoList().get(0);

            tvNode.setText(String.valueOf(info.getTtl()));
            tvNode.setSelected(TextUtils.isEmpty(pingInfo.getIp()));
            tvIP.setText(pingInfo.getIp());
            tvHost.setText(pingInfo.getHost());
            tvGeo.setText(pingInfo.getGeo());
            tvTime.setText(pingInfo.getTime());
        }
    }
}
