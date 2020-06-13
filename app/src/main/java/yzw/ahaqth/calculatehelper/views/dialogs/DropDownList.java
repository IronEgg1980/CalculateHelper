package yzw.ahaqth.calculatehelper.views.dialogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.views.MyDivideItemDecoration;
import yzw.ahaqth.calculatehelper.views.adapters.BaseAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.BaseViewHolder;

public class DropDownList<T> extends PopupWindow {
    private TextView anchorView;
    private List<T> mList;
    private BaseAdapter<T> adapter;

    public DropDownList(@NonNull final TextView anchorView, List<T> list){
        this.anchorView = anchorView;
        mList = list;
        adapter = new BaseAdapter<T>(R.layout.dropdown_list_item,mList) {
            @Override
            public void bindData(BaseViewHolder baseViewHolder, final T data) {
                baseViewHolder.setText(R.id.textview,data.toString());
                baseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        anchorView.setText(data.toString());
                        dismiss();
                    }
                });
            }
        };
        initialView();
        setTouchable(true);
        setOutsideTouchable(true);
    }

    private void initialView(){
        View view = LayoutInflater.from(anchorView.getContext()).inflate(R.layout.dropdown_popwindow,null);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(anchorView.getContext()));
        recyclerView.addItemDecoration(new MyDivideItemDecoration());
        recyclerView.setAdapter(adapter);
        setContentView(view);
    }
    public void show(){
        setWidth(anchorView.getWidth());
        setHeight(640);
        showAsDropDown(anchorView,-40,-40);
    }
}
