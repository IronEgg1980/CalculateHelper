package yzw.ahaqth.calculatehelper.views.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class BaseAdapter<T> extends RecyclerView.Adapter {
    private int layoutId;
    protected int parentWidth;
    protected List<T> mList;

    public BaseAdapter(int layoutId,List<T> list){
        this.layoutId = layoutId;
        this.mList = list;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindData((BaseViewHolder) holder,mList.get(position));
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parentWidth = parent.getMeasuredWidth();
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId,parent,false);
        return new BaseViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public abstract void bindData(BaseViewHolder baseViewHolder,T data);
}
