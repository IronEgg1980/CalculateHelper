package yzw.ahaqth.calculatehelper.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.RecordDetails;
import yzw.ahaqth.calculatehelper.moduls.RecordDetailsGroupByItem;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.MyDivideItemDecoration;

public class ShowInputResultFragment extends Fragment {
    public static ShowInputResultFragment newInstance(long recordTime) {
        Bundle args = new Bundle();
        args.putLong("recordtime",recordTime);
        ShowInputResultFragment fragment = new ShowInputResultFragment();
        fragment.setArguments(args);
        return fragment;
    }
    private LocalDateTime recordTime;
    private MyAdapter<RecordDetailsGroupByItem> resultAdapter;
    private List<RecordDetailsGroupByItem> resultList;

    private void initial(){
        resultList = DbManager.getRecordGroupByItem(recordTime);
        resultAdapter = new MyAdapter<RecordDetailsGroupByItem>(resultList) {
            @Override
            public void bindData(final MyAdapter.MyViewHolder myViewHolder, final RecordDetailsGroupByItem data) {
                myViewHolder.setText(R.id.itemname_textview,data.getItemName());
                myViewHolder.setText(R.id.amount_textview,String.valueOf(data.getTotalAmount()));
                myViewHolder.setText(R.id.note_textview,data.getMonthNote());
                final SwipeMenuLayout swipeMenuLayout = myViewHolder.getView(R.id.swipeMenuLayout);
                myViewHolder.getView(R.id.swipe_menu_del).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        swipeMenuLayout.smoothClose();
                        DbManager.deleInputRecord(data);
                        resultAdapter.notifyItemRemoved(myViewHolder.getAdapterPosition());
                        resultList.remove(data);
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_record_groupbyitem;
            }
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordTime = LocalDateTime.now();
        Bundle bundle = getArguments();
        if(bundle!=null){
            recordTime = LocalDateTime.ofEpochSecond(bundle.getLong("recordtime"),0,ZoneOffset.ofHours(8));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initial();
        View view = inflater.inflate(R.layout.single_recyclerview_layout,container,false);
        RecyclerView resultRecyclerView = view.findViewById(R.id.recyclerview);
        resultRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        resultRecyclerView.setAdapter(resultAdapter);
        resultRecyclerView.addItemDecoration(new MyDivideItemDecoration());
        return view;
    }
}
