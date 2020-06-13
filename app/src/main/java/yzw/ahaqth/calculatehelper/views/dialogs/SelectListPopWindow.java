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
import yzw.ahaqth.calculatehelper.views.MyDivideItemDecoration;
import yzw.ahaqth.calculatehelper.views.adapters.BaseAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.BaseViewHolder;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class SelectListPopWindow extends PopupWindow {
    private Activity mActivity;
    private List<String> list;
    private DialogCallback onItemSelected;
    private BaseAdapter<String> adapter;

    public SelectListPopWindow setOnItemSelected(DialogCallback onItemSelected) {
        this.onItemSelected = onItemSelected;
        return this;
    }

    public SelectListPopWindow(Activity activity, List<String> list) {
        this.list = list;
        this.mActivity = activity;
        initialView();
        setSize();
        setOutsideTouchable(true);
        setTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void initialView() {
        adapter = new BaseAdapter<String>(R.layout.dropdown_list_item,list) {
            @Override
            public void bindData(final BaseViewHolder baseViewHolder, final String data) {
                baseViewHolder.setText(R.id.textview,data);
                baseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        if (onItemSelected != null) {
                            onItemSelected.onDismiss(true,data);
                        }
                    }
                });
            }
        };
        View view = LayoutInflater.from(mActivity).inflate(R.layout.dropdown_popwindow,null);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.addItemDecoration(new MyDivideItemDecoration());
        recyclerView.setAdapter(adapter);
        setContentView(view);
    }

    private void setSize() {
        int sreenWidth = mActivity.getWindow().getDecorView().getWidth();
        int sreenHeight = mActivity.getWindow().getDecorView().getHeight();
        setWidth(sreenWidth * 4 / 5);
        setHeight(sreenHeight / 2);
    }

    public void show(){
        showAtLocation(mActivity.getWindow().getDecorView(), Gravity.CENTER,0,0);
    }
}
