package yzw.ahaqth.calculatehelper.views.dialogs;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.views.adapters.MyDivideItemDecoration;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class SelectListPopWindow extends PopupWindow {
    private Activity mActivity;
    private List<String> list;
    private DialogCallback onItemSelected;
    private MyAdapter<String> adapter;

    public SelectListPopWindow setOnItemSelected(DialogCallback onItemSelected) {
        this.onItemSelected = onItemSelected;
        return this;
    }

    public SelectListPopWindow(Activity activity, List<String> list) {
        this.list = list;
        this.mActivity = activity;
        initialView();
        setSize(mActivity.getWindow().getDecorView());
        setOutsideTouchable(true);
        setTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void initialView() {
        adapter = new MyAdapter<String>(list) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, final String data) {
                myViewHolder.setText(R.id.textview,data);
                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        if (onItemSelected != null) {
                            onItemSelected.onDismiss(true,data);
                        }
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_dropdown_list;
            }
        };
        View view = LayoutInflater.from(mActivity).inflate(R.layout.popwindow_dropdown_layout,null);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.addItemDecoration(new MyDivideItemDecoration());
        recyclerView.setAdapter(adapter);
        setContentView(view);
    }

    private SelectListPopWindow setSize(View anchor) {
//        int sreenWidth = mActivity.getWindow().getDecorView().getWidth();
//        int sreenHeight = mActivity.getWindow().getDecorView().getHeight();
//        setWidth(sreenWidth * 4 / 5);
//        setHeight(sreenHeight / 2);
        setWidth(anchor.getWidth());
        setHeight(1000);
        return this;
    }

    public void show(){
        showAtLocation(mActivity.getWindow().getDecorView(), Gravity.CENTER,0,0);
    }
}
