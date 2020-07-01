package yzw.ahaqth.calculatehelper.views;

import android.app.Activity;
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

import org.w3c.dom.Text;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.AssignDetails;
import yzw.ahaqth.calculatehelper.moduls.Person;
import yzw.ahaqth.calculatehelper.moduls.RecordDetails;
import yzw.ahaqth.calculatehelper.moduls.Remain;
import yzw.ahaqth.calculatehelper.moduls.RemainDetails;
import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.dialogs.LoadingDialog;
import yzw.ahaqth.calculatehelper.views.dialogs.ToastFactory;

public class AssignRemainFragment extends Fragment {
    public static AssignRemainFragment newInstance(long month) {
        Bundle args = new Bundle();
        args.putLong("month", month);
        AssignRemainFragment fragment = new AssignRemainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private List<AssignDetails> mList;
    private MyAdapter<AssignDetails> adapter;
    private LocalDate month;
    private double totalAmount;
    private AssignActivity activity;
    private final String SELECTION = "recordtime = ? and itemname = ?";
    private final String REMAIN_STRING = "***余额分配***";
    private Handler mHandler;
    private LoadingDialog loadingDialog;

    private TextView personSelectTextView;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        month = LocalDate.now();
        if(bundle!=null){
            month = LocalDate.ofEpochDay(bundle.getLong("month"));
        }
        activity = (AssignActivity) getActivity();
        totalAmount = DbManager.findFirst(Remain.class).getAmount();
        mList = DbManager.find(AssignDetails.class,SELECTION,  String.valueOf(activity.getRecordTime().toEpochSecond(ZoneOffset.ofHours(8))),REMAIN_STRING);
        adapter = new MyAdapter<AssignDetails>(mList) {
            @Override
            public void bindData(MyViewHolder myViewHolder, AssignDetails data) {

            }

            @Override
            public int getLayoutId(int position) {
                return 0;
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
                return true;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        loadingDialog = LoadingDialog.getInstance("正在保存...");
        View view = inflater.inflate(R.layout.fragment_remain_assign_layout,container,false);

        return view;
    }

    private void atuoAssign(@NonNull List<Person> people){
        if(people.isEmpty())
            return;
        double per = BigDecimalHelper.divideOnFloor(totalAmount,people.size());
        for (Person person : people) {
            AssignDetails assignDetails = new AssignDetails();
            assignDetails.setRecordTime(activity.getRecordTime());
            assignDetails.setMonth(month);
            assignDetails.setPersonName(person.getName());
            assignDetails.setAssignAmount(per);
            assignDetails.setOffDays(0);
            mList.add(assignDetails);
        }
    }

    private void assign(){
        loadingDialog.show(activity.getSupportFragmentManager(), "saveLoading");
        new Thread(new Runnable() {
            @Override
            public void run() {

                RemainDetails remainDetails = new RemainDetails();
                remainDetails.setRecordTime(activity.getRecordTime());
                remainDetails.setMonth(month);
//                remainDetails.setVariableAmount(getRemainValue());
                remainDetails.setVariableNote(REMAIN_STRING);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                if (DbManager.saveAssignData(list, remainDetails)) {
//                    mHandler.sendEmptyMessage(0x01);
//                } else {
//                    mHandler.sendEmptyMessage(0x02);
//                }
            }
        }).start();
    }

    private void clearAssign(){

    }
}
