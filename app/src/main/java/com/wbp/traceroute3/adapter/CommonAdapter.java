package com.wbp.traceroute3.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wbp on 2017/7/21.
 */

public abstract class CommonAdapter<DATA> extends RecyclerView.Adapter<CommonHolder> {
    protected List<DATA> datas = new ArrayList<>();
    protected HolderFactory holderFactory;

    public abstract void initHolders();

    public CommonAdapter() {
        initHolders();
    }

    public List<DATA> getDatas() {
        return datas;
    }

    public void setDatas(List<DATA> datas) {
        if (datas == null) return;
        this.datas.clear();
        this.datas.addAll(datas);
        notifyDataSetChanged();
    }

    public void addDatasHead(List<DATA> datas) {
        if (datas == null) return;
        this.datas.addAll(0, datas);
        notifyDataSetChanged();
    }

    public void addDatasTail(List<DATA> datas) {
        if (datas == null) return;
        this.datas.addAll(datas);
        notifyItemRangeChanged(this.datas.size() - datas.size(), datas.size());
//        notifyDataSetChanged();
    }

    public void addOneTail(DATA data) {
        if (data == null) return;
        this.datas.add(data);
        notifyItemInserted(this.datas.size() - 1);
//        notifyDataSetChanged();
    }

    @Override
    public CommonHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return holderFactory.createHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(CommonHolder holder, int position) {
        holder.bind(datas, position);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    /**
     * 如果有多个类型,则需要重写此方法
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    class HolderFactory {
        private SparseArray<Class> holders;

        public HolderFactory(SparseArray<Class> holders) {
            this.holders = holders;
        }

        private Class getHolderClass(int viewType) {
            // 只有一种类型的时候, 没有重写getItemViewType, 所以直接返回现有的Holder即可
            if (holders.size() == 1) {
                return holders.valueAt(0);
            }
            return holders.get(viewType);
        }

        private int getResId(int viewType) {
            if (holders.size() == 1) {
                return holders.keyAt(0);
            }
            return viewType;
        }

        public CommonHolder createHolder(@NonNull ViewGroup parent, int viewType) {
            try {
                Class<?> holderClass = getHolderClass(viewType);
                Constructor holderConstructor = holderClass.getConstructor(CommonAdapter.this.getClass(), View.class);
                View view = LayoutInflater.from(parent.getContext()).inflate(getResId(viewType), parent, false);
                return ((CommonHolder) (holderConstructor.newInstance(CommonAdapter.this, view)));
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | NullPointerException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
