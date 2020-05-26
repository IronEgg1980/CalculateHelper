package yzw.ahaqth.calculatehelper.views;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.AssignGroupByPerson;
import yzw.ahaqth.calculatehelper.moduls.RecordDetailsGroupByItem;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.BaseAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.BaseViewHolder;

public class ShowRecordDetailsActivity extends AppCompatActivity {
    private LocalDateTime recordTime;
    private RecyclerView recyclerView;
    private TabLayout tabLayout;
    private BaseAdapter<RecordDetailsGroupByItem> recordGroupByItemAdapter;
    private BaseAdapter<AssignGroupByPerson> assignAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_record_details);
        Bundle bundle = getIntent().getExtras();
        recordTime = LocalDateTime.now();
        if(bundle != null){
            recordTime = LocalDateTime.ofEpochSecond(bundle.getLong("recordtime"),0, ZoneOffset.ofHours(8));
        }
        initial();
        initialView();
    }

    private void initial(){
        List<RecordDetailsGroupByItem> list = DbManager.getRecordGroupByItem(recordTime);
        recordGroupByItemAdapter = new BaseAdapter<RecordDetailsGroupByItem>(R.layout.recordgroupbyitem_item_layout, list) {
            @Override
            public void bindData(final BaseViewHolder baseViewHolder, final RecordDetailsGroupByItem data) {
                baseViewHolder.setText(R.id.itemname_textview, data.getItemName());
                baseViewHolder.setText(R.id.amount_textview, "总金额：" + data.getTotalAmount());
                baseViewHolder.setText(R.id.note_textview, data.getMonthNote());
            }
        };

        List<AssignGroupByPerson> list2 = DbManager.getAssignGroupByPersonList(recordTime);
        assignAdapter = new BaseAdapter<AssignGroupByPerson>(R.layout.assign_recorddetailslist_item, list2) {
            @Override
            public void bindData(BaseViewHolder baseViewHolder, AssignGroupByPerson data) {
                baseViewHolder.setText(R.id.monthTextView, data.getPersonName());
                baseViewHolder.setText(R.id.amountTextView, "总金额：" + data.getAssignAmount());
                baseViewHolder.setText(R.id.noteTextView, "明细：" + data.getMonthList());
                baseViewHolder.setText(R.id.datamodeTextView, data.getOffDaysNote());
            }
        };
    }

    private void initialView(){
        TextView titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText("查看记录详情");
        findViewById(R.id.navagationIco).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0){
                    recyclerView.setAdapter(recordGroupByItemAdapter);
                }else{
                    recyclerView.setAdapter(assignAdapter);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new MyDivideItemDecoration());
        recyclerView.setAdapter(recordGroupByItemAdapter);
    }
}
