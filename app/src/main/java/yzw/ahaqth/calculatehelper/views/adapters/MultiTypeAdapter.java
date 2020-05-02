package yzw.ahaqth.calculatehelper.views.adapters;

import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;

public abstract class MultiTypeAdapter extends BaseAdapter<MultiTypeModul>{
    public MultiTypeAdapter(List<MultiTypeModul> list) {
        super(-1,list);
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).getItemViewType();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = ItemViewTypeSupport.getLayoutId(viewType);
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId,parent,false);
        return new BaseViewHolder(view);
    }
}
