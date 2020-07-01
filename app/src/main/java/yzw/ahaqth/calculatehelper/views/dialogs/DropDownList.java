package yzw.ahaqth.calculatehelper.views.dialogs;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.views.adapters.MyDivideItemDecoration;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;

public class DropDownList<T> extends PopupWindow {
    private TextView anchorView;
    private List<T> mList;
    private MyAdapter<T> adapter;

    public DropDownList(@NonNull final TextView anchorView, List<T> list){
        this.anchorView = anchorView;
        mList = list;
        adapter = new MyAdapter<T>(mList) {
            @Override
            public void bindData(MyViewHolder myViewHolder, final T data) {
                myViewHolder.setText(R.id.textview,data.toString());
                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        anchorView.setText(data.toString());
                        dismiss();
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_dropdown_list;
            }
        };
        initialView();
        setTouchable(true);
        setOutsideTouchable(true);
    }

    private void initialView(){
        View view = LayoutInflater.from(anchorView.getContext()).inflate(R.layout.popwindow_dropdown_layout,null);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(anchorView.getContext()));
        recyclerView.addItemDecoration(new MyDivideItemDecoration());
        recyclerView.setAdapter(adapter);
        setContentView(view);
    }
    public void show(){
        setWidth(anchorView.getWidth());
        setHeight(1000);
        showAsDropDown(anchorView,0,-30);
    }
}
