package yzw.ahaqth.calculatehelper.views.dialogs;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.TransferQueue;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.Remain;
import yzw.ahaqth.calculatehelper.moduls.RemainDetails;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.MyDivideItemDecoration;

public class ShowRemainDetailsPop extends PopupWindow {
    private Activity mActivity;
    private List<RemainDetails> remainList;
    private MyAdapter<RemainDetails> adapter;
    public ShowRemainDetailsPop(Activity activity){
        mActivity = activity;
        initial();
        setView();
        setTouchable(true);
        setOutsideTouchable(true);
        setFocusable(true);
        Point point = new Point();
        activity.getWindowManager().getDefaultDisplay().getRealSize(point);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        setHeight((int) (point.y*0.8));
        setAnimationStyle(R.style.PopwindowAnimBottomTop);
    }

    private void initial() {
        remainList = DbManager.find(RemainDetails.class,
                false,
                "variableamount != ?",
                new String[]{String.valueOf(0)},
                null,
                null,
                "month");
        adapter = new MyAdapter<RemainDetails>(remainList) {
            @Override
            public void bindData(MyViewHolder myViewHolder, RemainDetails data) {
                String text = data.getMonth().format(DateUtils.getYyyyM_Formatter()) + "（"+data.getVariableNote()+")";
                myViewHolder.setText(R.id.monthTextView, text);
                double amount = data.getVariableAmount();
                TextView textView = myViewHolder.getView(R.id.amountTextView);
                String amountString = amount > 0 ? "+" + amount : String.valueOf(amount);
                textView.setTextColor(amount > 0 ? Color.GREEN : Color.RED);
                textView.setText(amountString);
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_remaindetails;
            }
        };
    }
    
    private void setView(){
        View view = LayoutInflater.from(mActivity).inflate(R.layout.popwindow_show_remaindetails,new LinearLayout(mActivity),false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.addItemDecoration(new MyDivideItemDecoration());
        recyclerView.setAdapter(adapter);
        view.findViewById(R.id.close_view) .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setContentView(view);
    }
    public void show(){
        showAtLocation(mActivity.getWindow().getDecorView(), Gravity.BOTTOM,0,0);
    }
}
