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
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.AssignDetails;
import yzw.ahaqth.calculatehelper.moduls.Person;
import yzw.ahaqth.calculatehelper.moduls.Record;
import yzw.ahaqth.calculatehelper.moduls.RecordDetails;
import yzw.ahaqth.calculatehelper.moduls.Remain;
import yzw.ahaqth.calculatehelper.moduls.RemainDetails;
import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.MyDivideItemDecoration;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;
import yzw.ahaqth.calculatehelper.views.dialogs.InputNumberDialog;
import yzw.ahaqth.calculatehelper.views.dialogs.LoadingDialog;
import yzw.ahaqth.calculatehelper.views.dialogs.SelectPersonPopWindow;
import yzw.ahaqth.calculatehelper.views.dialogs.ShowRemainDetailsPop;
import yzw.ahaqth.calculatehelper.views.dialogs.ToastFactory;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class AssignRemainFragment extends Fragment {
    private List<AssignDetails> mList;
    private MyAdapter<AssignDetails> adapter;
    private LocalDate month;
    private double totalAmount;
    private AssignActivity activity;
    private final String REMAIN_STRING = "***余额分配***";
    private Handler mHandler;
    private LoadingDialog loadingDialog;

    private TextView personSelectTextView,totalAmountTextView;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        month = LocalDate.now();
        activity = (AssignActivity) getActivity();
        RecordDetails recordDetails = DbManager.findOne(RecordDetails.class,"recordtime = ?",
                String.valueOf(activity.getRecordTime().toEpochSecond(ZoneOffset.ofHours(8))));
        if(recordDetails!=null){
            month = recordDetails.getMonth();
        }
        totalAmount = DbManager.findFirst(Remain.class).getAmount();
        String selection = "recordtime = ? and note = ?";
        mList = DbManager.find(AssignDetails.class, selection,  String.valueOf(activity.getRecordTime().toEpochSecond(ZoneOffset.ofHours(8))),REMAIN_STRING);
        adapter = new MyAdapter<AssignDetails>(mList) {
            @Override
            public void bindData(MyViewHolder myViewHolder, AssignDetails data) {
                myViewHolder.setText(R.id.monthTextView,data.getPersonName());
                myViewHolder.setText(R.id.amountTextView,String.valueOf(data.getAssignAmount()));
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_remaindetails;
            }
        };
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                loadingDialog.dismiss();
                if (msg.what == 0x01) {
                    ToastFactory.showCenterToast(getContext(), "数据已保存");
                    adapter.notifyDataSetChanged();
                } else if (msg.what == 0x02) {
                    ToastFactory.showCenterToast(getContext(), "保存失败");
                }
                showInfo();
                return true;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        loadingDialog = LoadingDialog.getInstance("正在保存...");
        View view = inflater.inflate(R.layout.fragment_remain_assign_layout,container,false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new MyDivideItemDecoration());
        totalAmountTextView = view.findViewById(R.id.totalAmountTextView);
        totalAmountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemainDetails();
            }
        });
        personSelectTextView = view.findViewById(R.id.selectPersonTextView);
        personSelectTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPerson();
            }
        });
        view.findViewById(R.id.justifyRemainView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                justifyRemainValue();
            }
        });
        view.findViewById(R.id.clearAssignView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAssign();
            }
        });
        showInfo();
        return view;
    }

    private void showInfo(){
        String title = "总余额："+totalAmount;
        String personString =mList.isEmpty()?"点击选择分配人员...": "分配人数："+mList.size()+" 人，点击重新分配...";
        totalAmountTextView.setText(title);
        personSelectTextView.setText(personString);
    }

    private void justifyRemainValue(){
        if(mList.isEmpty()){
            InputNumberDialog inputNumberDialog = InputNumberDialog.getInstance(Integer.MAX_VALUE,totalAmount);
            inputNumberDialog.setOnDismiss(new DialogCallback() {
                @Override
                public void onDismiss(boolean confirmFlag, Object... values) {
                    if(confirmFlag){
                        double value = (double) values[0];
                        double valueDiff = BigDecimalHelper.minus(value,totalAmount);
                        Remain remain = DbManager.findFirst(Remain.class);
                        remain.setAmount(value);
                        remain.update();

                        RemainDetails remainDetails = new RemainDetails();
                        remainDetails.setRecordTime(activity.getRecordTime());
                        remainDetails.setMonth(month);
                        remainDetails.setVariableAmount(valueDiff);
                        remainDetails.setVariableNote("***余额调整***");
                        remainDetails.save();

                        totalAmount = value;
                        showInfo();
                    }
                }
            });
            inputNumberDialog.show(activity.getSupportFragmentManager(),"input");
        }else{
            DialogFactory dialogFactory = DialogFactory.getConfirmDialog("存在余额分配记录，是否撤销分配并调整余额？");
            dialogFactory.setDialogCallback(new DialogCallback() {
                @Override
                public void onDismiss(boolean confirmFlag, Object... values) {
                    if(confirmFlag){
                        clearAssign();
                        justifyRemainValue();
                    }
                }
            }).show(activity.getSupportFragmentManager(),"confirm");
        }
    }

    private void selectPerson(){
        if(mList.isEmpty()) {
            new SelectPersonPopWindow(activity)
                    .setOnCallBack(new DialogCallback() {
                        @Override
                        public void onDismiss(boolean confirmFlag, Object... values) {
                            if (confirmFlag) {
                                List<Person> list = new ArrayList<>();
                                for (Object o : values) {
                                    list.add((Person) o);
                                }
                                atuoAssign(list);
                            }
                        }
                    })
                    .show();
        }else{
            DialogFactory dialogFactory = DialogFactory.getConfirmDialog("存在余额分配记录，是否重新分配？");
            dialogFactory.setDialogCallback(new DialogCallback() {
                @Override
                public void onDismiss(boolean confirmFlag, Object... values) {
                    if(confirmFlag){
                        clearAssign();
                    }
                }
            }).show(activity.getSupportFragmentManager(),"confirm");
        }
    }


    private void showRemainDetails(){
        new ShowRemainDetailsPop(activity)
                .show();
    }

    private void atuoAssign(@NonNull List<Person> people){
        if(people.isEmpty() || BigDecimalHelper.minus(totalAmount,people.size()) < 1) {
            ToastFactory.showCenterToast(getContext(),"未选择人员或余额太少不够分配");
            return;
        }
        double per = BigDecimalHelper.divideOnFloor(totalAmount,people.size());
        mList.clear();
        for (Person person : people) {
            AssignDetails assignDetails = new AssignDetails();
            assignDetails.setRecordTime(activity.getRecordTime());
            assignDetails.setMonth(month);
            assignDetails.setPersonName(person.getName());
            assignDetails.setAssignAmount(per);
            assignDetails.setOffDays(0);
            assignDetails.setNote(REMAIN_STRING);
            mList.add(assignDetails);
        }
        save();
    }

    private double getAssignValue(){
        double d = 0;
        for(AssignDetails details:mList){
            d = BigDecimalHelper.add(d,details.getAssignAmount());
        }
        return d;
    }

    private void save(){
        loadingDialog.show(activity.getSupportFragmentManager(), "saveLoading");
        new Thread(new Runnable() {
            @Override
            public void run() {
                double assignedValue = getAssignValue();
                RemainDetails remainDetails = new RemainDetails();
                remainDetails.setRecordTime(activity.getRecordTime());
                remainDetails.setMonth(month);
                remainDetails.setVariableAmount(-assignedValue);
                remainDetails.setVariableNote(REMAIN_STRING);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (DbManager.saveAssignRemainData(mList, remainDetails)) {
                    totalAmount = BigDecimalHelper.minus(totalAmount,assignedValue);
                    mHandler.sendEmptyMessage(0x01);
                } else {
                    mHandler.sendEmptyMessage(0x02);
                }
            }
        }).start();
    }

    private void clearAssign(){
        if(mList.isEmpty())
            return;
        if(DbManager.clearRemainAssign(mList)){
            ToastFactory.showCenterToast(getContext(),"已撤销所有分配");
            mList.clear();
            totalAmount = DbManager.findFirst(Remain.class).getAmount();
            adapter.notifyDataSetChanged();
            showInfo();
        }else{
            ToastFactory.showCenterToast(getContext(),"撤销操作失败");
        }
    }
}
