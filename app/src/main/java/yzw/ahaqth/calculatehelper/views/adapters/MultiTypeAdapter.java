package yzw.ahaqth.calculatehelper.views.adapters;

import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;

public abstract class MultiTypeAdapter<T extends ItemViewTypeLayoutConverter> extends BaseAdapter<T>{
    private SparseIntArray layoutIds;

    public MultiTypeAdapter(List<T> list) {
        super(-1,list);
        this.layoutIds = new SparseIntArray();
    }

    @Override
    public int getItemViewType(int position) {
        T t = mList.get(position);
        layoutIds.put(t.getItemViewType(),t.getLayoutId());
        return t.getItemViewType();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = layoutIds.get(viewType);
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId,parent,false);
        return new BaseViewHolder(view);
    }
}
