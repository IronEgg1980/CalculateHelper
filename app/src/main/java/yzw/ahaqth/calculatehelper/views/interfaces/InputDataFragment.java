package yzw.ahaqth.calculatehelper.views.interfaces;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.RecordDetails;
import yzw.ahaqth.calculatehelper.moduls.RecordDetailsGroupByItem;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.views.MyDivideItemDecoration;
import yzw.ahaqth.calculatehelper.views.WorkMainActivity;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;

public class InputDataFragment extends Fragment {
    private WorkMainActivity activity;
    private MaterialEditText itemNameET,amountET;
    private RecyclerView resultRecyclerView,monthListRecyclerView;
    private List<RecordDetails> recordDetailsList;
    private List<RecordDetailsGroupByItem> resultList;
    private MyAdapter<RecordDetails> monthListAdapter;
    private MyAdapter<RecordDetailsGroupByItem> resultAdapter;

    private void initial(){
        activity = (WorkMainActivity) getActivity();
        resultList = new ArrayList<>();
        initialRecordDetailsList();

        monthListAdapter = new MyAdapter<RecordDetails>(recordDetailsList) {
            @Override
            public void bindData(MyViewHolder myViewHolder, RecordDetails data) {
                if(data.getDataMode() == DataMode.EMPTY){

                }else{
                    myViewHolder.setText(R.id.monthTextView,data.getMonth().format(DateUtils.getYyM_Formatter()));
                    myViewHolder.setText(R.id.amountTextView,String.valueOf(data.getAmount()));
                }
            }

            @Override
            public int getLayoutId(int position) {
                if(recordDetailsList.get(position).getDataMode()==DataMode.EMPTY)
                    return R.layout.select_month_item_addbutton;
                return R.layout.select_month_item;
            }
        };

        resultAdapter = new MyAdapter<RecordDetailsGroupByItem>(resultList) {
            @Override
            public void bindData(MyViewHolder myViewHolder, RecordDetailsGroupByItem data) {

            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.recordgroupbyitem_item_layout;
            }
        };
    }

    private void initialRecordDetailsList(){
        recordDetailsList = new ArrayList<>();
        LocalDate month = LocalDate.now();
        for(int i = 0;i<6;i++){
            RecordDetails recordDetails = new RecordDetails();
            recordDetails.setRecordTime(activity.getRecordTime());
            recordDetails.setMonth(month.minusMonths(i));
            recordDetails.setAmount(0);
            recordDetails.setDataMode(DataMode.UNASSIGNED);
            recordDetails.setItemName("");
            recordDetailsList.add(recordDetails);
        }
        RecordDetails recordDetails2 = new RecordDetails();
        recordDetails2.setDataMode(DataMode.EMPTY);
        recordDetailsList.add(recordDetails2);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initial();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input_data_layout,container,false);
        view.findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.changeToAssign();// 进入下一步
            }
        });
        itemNameET = view.findViewById(R.id.itemname_textview);
        amountET = view.findViewById(R.id.amount_textview);
        monthListRecyclerView = view.findViewById(R.id.month_list_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        monthListRecyclerView.setLayoutManager(linearLayoutManager);
        monthListRecyclerView.setAdapter(monthListAdapter);

        resultRecyclerView = view.findViewById(R.id.result_recyclerview);
        resultRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        resultRecyclerView.setAdapter(resultAdapter);
        resultRecyclerView.addItemDecoration(new MyDivideItemDecoration());
        return view;
    }
}
