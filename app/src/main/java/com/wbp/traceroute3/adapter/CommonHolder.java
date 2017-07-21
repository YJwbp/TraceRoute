package com.wbp.traceroute3.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by wbp on 2017/7/21.
 */

public abstract class CommonHolder<T extends List<?>> extends RecyclerView.ViewHolder {
    /**
     * 新Holder 需要修改资源ID
     */

    public CommonHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(final T datas, final int position) {

    }

}