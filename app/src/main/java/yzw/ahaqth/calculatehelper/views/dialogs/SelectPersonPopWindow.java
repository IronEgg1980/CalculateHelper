package yzw.ahaqth.calculatehelper.views.dialogs;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.Person;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class SelectPersonPopWindow extends PopupWindow {
    private List<Person> mList;
    private DialogCallback onCallBack;
    private Activity mActivity;

    public SelectPersonPopWindow(Activity activity) {
        mActivity = activity;
        Point point = new Point();
        activity.getWindowManager().getDefaultDisplay().getRealSize(point);
        setView(LayoutInflater.from(activity), new LinearLayout(activity));
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight((int) (point.y * 0.7));
        setOutsideTouchable(true);
        setTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setFocusable(true);
        setAnimationStyle(R.style.PopwindowAnimBottomTop);
    }

    public void show() {
        showAtLocation(mActivity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 50);
    }

    public SelectPersonPopWindow setOnCallBack(DialogCallback onCallBack) {
        this.onCallBack = onCallBack;
        return this;
    }

    private void confirm() {
        if (onCallBack == null)
            return;

        List<Person> result = new ArrayList<>();
        for (Person p : mList) {
            if (p.isSelected)
                result.add(p);
        }
        if (result.isEmpty())
            onCallBack.onDismiss(false);
        else
            onCallBack.onDismiss(true, result.toArray());
    }

    private void selectAll(){
        for(Person p : mList)
            p.isSelected = true;
    }

    private void selectReverse(){
        for(Person p : mList)
            p.isSelected = !p.isSelected;
    }


    public void setView(LayoutInflater inflater, final ViewGroup container) {
        mList = DbManager.findAll(Person.class);

        View view = inflater.inflate(R.layout.popwindow_select_person, container, false);
        final MyAdapter<Person> adapter = new MyAdapter<Person>(mList) {
            @Override
            public void bindData(MyViewHolder myViewHolder, final Person data) {
                final int index = myViewHolder.getAdapterPosition();
                final boolean isSelected = data.isSelected;
                myViewHolder.setText(R.id.item_person_name, data.getName());
                myViewHolder.getView(R.id.selected_flag).setVisibility(isSelected ? View.VISIBLE : View.GONE);
                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        data.isSelected = !isSelected;
                        notifyItemChanged(index);
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_person_select;
            }
        };

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(mActivity, 2));
        recyclerView.setAdapter(adapter);
        view.findViewById(R.id.close_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        view.findViewById(R.id.confirm_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                confirm();
            }
        });
        view.findViewById(R.id.select_all_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               selectAll();
               adapter.notifyDataSetChanged();
            }
        });
        view.findViewById(R.id.select_reverse_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectReverse();
                adapter.notifyDataSetChanged();
            }
        });
        setContentView(view);
    }
}
