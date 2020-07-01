package yzw.ahaqth.calculatehelper.views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.RecordDetailsGroupByMonth;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.MyDivideItemDecoration;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;
import yzw.ahaqth.calculatehelper.views.dialogs.ToastFactory;
import yzw.ahaqth.calculatehelper.views.interfaces.DataMode;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class AssignAmountFragment extends Fragment {
    private static final String TAG = "殷宗旺";
    private List<RecordDetailsGroupByMonth> mList;
    private MyAdapter<RecordDetailsGroupByMonth> adapter;
    private AssignActivity activity;

    private void assign(final MyAdapter.MyViewHolder myViewHolder, final RecordDetailsGroupByMonth data) {
        if (data.getDataMode() == DataMode.ASSIGNED) {
            DialogFactory dialogFactory = DialogFactory.getConfirmDialog("该月份已分配，是否要重新分配？");
            dialogFactory.setDialogCallback(new DialogCallback() {
                @Override
                public void onDismiss(boolean confirmFlag, Object... values) {
                    if (confirmFlag) {
                        if (DbManager.rollBackAssign(data.getRecordTime(), data.getMonth())) {
                            data.setDataMode(DataMode.UNASSIGNED);
                            assign(myViewHolder, data);
                        } else {
                            ToastFactory.showCenterToast(getContext(), "操作失败");
                        }
                    }
                }
            });
            dialogFactory.show(activity.getSupportFragmentManager(), "reassign");
        } else {
            activity.clickItemIndex = myViewHolder.getAdapterPosition();
            double value = data.getTotalAmount();
            long month = data.getMonth().toEpochDay();
            activity.showAssignFragment(value,month);
        }
    }

    private void onItemAssigned() {
        Log.d(TAG, "onItemAssigned: 1");
        if (activity.isAssigned && activity.clickItemIndex != -1) {
            Log.d(TAG, "onItemAssigned: 2");
            mList.get(activity.clickItemIndex).setDataMode(DataMode.ASSIGNED);
            adapter.notifyItemChanged(activity.clickItemIndex);

            activity.isAssigned = false;
            activity.clickItemIndex = -1;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AssignActivity) getActivity();
        activity.isAssigned = false;
        activity.clickItemIndex = -1;
        mList = DbManager.getRecordGroupByMonth(activity.getRecordTime());
        adapter = new MyAdapter<RecordDetailsGroupByMonth>(mList) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, final RecordDetailsGroupByMonth data) {
                myViewHolder.setText(R.id.monthTextView, data.getMonth().format(DateUtils.getYyyyM_Formatter()));
                myViewHolder.setText(R.id.amountTextView, String.valueOf(data.getTotalAmount()));
                myViewHolder.setText(R.id.noteTextView, data.getItemNote());
                myViewHolder.setText(R.id.datamodeTextView, data.getDataMode().getDescribe());
                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        assign(myViewHolder, data);
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_assign_record_details;
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.single_recyclerview_layout, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new MyDivideItemDecoration());
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            onItemAssigned();
        }
    }
}
