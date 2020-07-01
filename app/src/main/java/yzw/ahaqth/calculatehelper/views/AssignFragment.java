package yzw.ahaqth.calculatehelper.views;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.AssignDetails;
import yzw.ahaqth.calculatehelper.moduls.Person;
import yzw.ahaqth.calculatehelper.moduls.RecordDetailsGroupByMonth;
import yzw.ahaqth.calculatehelper.moduls.RemainDetails;
import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.MyDivideItemDecoration;
import yzw.ahaqth.calculatehelper.views.dialogs.InputNumberDialog;
import yzw.ahaqth.calculatehelper.views.dialogs.LoadingDialog;
import yzw.ahaqth.calculatehelper.views.dialogs.SelectPersonPopWindow;
import yzw.ahaqth.calculatehelper.views.dialogs.ToastFactory;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class AssignFragment extends Fragment {
    private static final String TAG = "殷宗旺";

    public static AssignFragment newInstance(double value, long month) {
        Bundle args = new Bundle();
        args.putDouble("value", value);
        args.putLong("month", month);
        AssignFragment fragment = new AssignFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private double totalAmount = 0;
    private LocalDate month = LocalDate.now();
    private AssignActivity activity;
    private List<Person> mList;
    private MyAdapter<Person> adapter;
    private TextView personSelectTextView;
    private Handler mHandler;
    private LoadingDialog loadingDialog;


    private void saveResult() {
        if(mList.isEmpty()){
            ToastFactory.showCenterToast(getContext(),"请先选择人员完成分配，再保存数据");
            return;
        }
        loadingDialog.show(activity.getSupportFragmentManager(), "saveLoading");
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<AssignDetails> list = new ArrayList<>();
                for (Person person : mList) {
                    AssignDetails assignDetails = new AssignDetails();
                    assignDetails.setRecordTime(activity.getRecordTime());
                    assignDetails.setMonth(month);
                    assignDetails.setPersonName(person.getName());
                    assignDetails.setAssignAmount(person.assignAmout);
                    assignDetails.setOffDays(person.offDays);
                    list.add(assignDetails);
                }
                RemainDetails remainDetails = new RemainDetails();
                remainDetails.setRecordTime(activity.getRecordTime());
                remainDetails.setMonth(month);
                remainDetails.setVariableAmount(getRemainValue());
                remainDetails.setVariableNote("***分配结余***");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (DbManager.saveAssignData(list, remainDetails)) {
                    mHandler.sendEmptyMessage(0x01);
                } else {
                    mHandler.sendEmptyMessage(0x02);
                }
            }
        }).start();
    }

    private void onSaved() {
        activity.isAssigned = true;
        activity.changeToTab1();
    }

    private double getRemainValue() {
        double remainValue = totalAmount;
        for (Person p : mList) {
            remainValue = BigDecimalHelper.minus(remainValue, p.assignAmout);
        }
        return remainValue;
    }

    private void atuoAssign() {
        double totalRatio = 0;
        for (Person p : mList) {
            totalRatio = BigDecimalHelper.add(totalRatio, p.assignRatio);
            p.assignAmout = 0;
        }

        if (totalRatio != 0) {
            double per = BigDecimalHelper.divide(totalAmount, totalRatio);
            for (Person p : mList) {
                p.assignAmout = BigDecimalHelper.multiplyOnFloor(per, p.assignRatio);
            }
        }
    }

    private void showPersonCountInfo() {
        String text = mList.isEmpty() ? "点击选择参与分配人员..." : "当前参与分配人数：" + mList.size() + " 人，点击重新选择...";
        personSelectTextView.setText(text);
    }

    private void inputOffDays(final Person data){
        final int maxDay = month.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
        InputNumberDialog inputNumberDialog = InputNumberDialog.getInstance(maxDay,data.offDays,"请输入请假天数","本月最大天数：","请假天数：");
        inputNumberDialog.setOnDismiss(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                double value = (double) values[0];
                if(confirmFlag){
                    data.offDays = value;
                    data.assignRatio = BigDecimalHelper.divide(BigDecimalHelper.minus(maxDay,value),maxDay,2);
                    atuoAssign();
                    adapter.notifyDataSetChanged();
                }
            }
        });
        inputNumberDialog.show(activity.getSupportFragmentManager(),"inputOffdays");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            totalAmount = bundle.getDouble("value");
            month = LocalDate.ofEpochDay(bundle.getLong("month"));
        }
        activity = (AssignActivity) getActivity();
        mList = new ArrayList<>();
        adapter = new MyAdapter<Person>(mList) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, final Person data) {
                myViewHolder.setText(R.id.nameTextView, data.getName());
                myViewHolder.setText(R.id.assignAmountTextView, "当前分配：" + data.assignAmout);
                myViewHolder.setText(R.id.offDays, "请假：" + data.offDays);
                myViewHolder.setText(R.id.assignRatio, "分配系数：" + data.assignRatio);
                myViewHolder.getView(R.id.offDays).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        inputOffDays(data);
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_on_assign_work;
            }
        };
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                loadingDialog.dismiss();
                if (msg.what == 0x01) {
                    ToastFactory.showCenterToast(getContext(), "数据已保存");
                    onSaved();
                } else if (msg.what == 0x02) {
                    ToastFactory.showCenterToast(getContext(), "保存失败");
                }
                return true;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assign, container, false);
        personSelectTextView = view.findViewById(R.id.selectPersonTextView);
        personSelectTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SelectPersonPopWindow(activity)
                        .setOnCallBack(new DialogCallback() {
                            @Override
                            public void onDismiss(boolean confirmFlag, Object... values) {
                                if (confirmFlag) {
                                    mList.clear();
                                    for (Object o : values) {
                                        mList.add((Person) o);
                                    }
                                    atuoAssign();
                                    adapter.notifyDataSetChanged();
                                    showPersonCountInfo();
                                }
                            }
                        })
                        .show();
            }
        });
        view.findViewById(R.id.cancelView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.isAssigned = false;
                activity.changeToTab1();
            }
        });
        view.findViewById(R.id.confirmView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveResult();
            }
        });
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new MyDivideItemDecoration());

        TextView textView = view.findViewById(R.id.totalAmountTextView);
        String s = "总金额：" + totalAmount;
        textView.setText(s);

        loadingDialog = LoadingDialog.getInstance("正在保存...");

        return view;
    }
}
