package yzw.ahaqth.calculatehelper.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.AssignGroupByPerson;
import yzw.ahaqth.calculatehelper.moduls.RecordDetailsGroupByItem;
import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.tools.Tools;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;

public class Infomation extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TabLayout tabLayout;
    private List<RecordDetailsGroupByItem> recordDetailsList;
    private List<AssignGroupByPerson> assignList;
    private double maxAmount = 0, maxAssignAmount = 0;
    private MyAdapter<RecordDetailsGroupByItem> recordAdapter;
    private MyAdapter<AssignGroupByPerson> assignAdapter;
    private int step = 1,currentTabPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infomation);
        initial();
        initialView();
    }

    @Override
    public void onBackPressed() {
        if(step == 1)
            super.onBackPressed();
        else{
            step = 1;
            changeAdapter();
        }
    }

    private void initial() {
        recordDetailsList = DbManager.getInfomationList();
        assignList = DbManager.getAssignInformationList();
        Collections.sort(recordDetailsList, new Comparator<RecordDetailsGroupByItem>() {
            @Override
            public int compare(RecordDetailsGroupByItem o1, RecordDetailsGroupByItem o2) {
                return Double.compare(o2.getTotalAmount(), o1.getTotalAmount());
            }
        });
        Collections.sort(assignList, new Comparator<AssignGroupByPerson>() {
            @Override
            public int compare(AssignGroupByPerson o1, AssignGroupByPerson o2) {
                return Double.compare(o2.getAssignAmount(), o1.getAssignAmount());
            }
        });
        maxAmount = recordDetailsList.isEmpty() ? 1 : recordDetailsList.get(0).getTotalAmount();
        maxAssignAmount = assignList.isEmpty() ? 1 : assignList.get(0).getAssignAmount();
        recordAdapter = new MyAdapter<RecordDetailsGroupByItem>(recordDetailsList) {
            @Override
            public int getLayoutId(int position) {
                return R.layout.infomation_record_item;
            }

            @Override
            public void bindData(final MyViewHolder myViewHolder, RecordDetailsGroupByItem data) {
                myViewHolder.setText(R.id.amountTV, String.valueOf(data.getTotalAmount()));
                myViewHolder.setText(R.id.itemnameTV, data.getItemName());
                double ratio = BigDecimalHelper.divide(data.getTotalAmount(), maxAmount);
                View view = myViewHolder.getView(R.id.progressTV);
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.width = (int) ((parentWidth - Tools.dip2px(Infomation.this, 100)) * ratio);
                view.setLayoutParams(layoutParams);

                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRecordItemClick(myViewHolder.getAdapterPosition());
                    }
                });
            }
        };
        assignAdapter = new MyAdapter<AssignGroupByPerson>(assignList) {
            @Override
            public int getLayoutId(int position) {
                return R.layout.info_details_item;
            }

            @Override
            public void bindData(final MyViewHolder myViewHolder, AssignGroupByPerson data) {
                myViewHolder.setText(R.id.amountTextView, String.valueOf(data.getAssignAmount()));
                myViewHolder.setText(R.id.monthTextView, data.getPersonName());
                double ratio = BigDecimalHelper.divide(data.getAssignAmount(), maxAssignAmount);
                View view = myViewHolder.getView(R.id.progressView);
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.width = (int) ((parentWidth - Tools.dip2px(Infomation.this, 160)) * ratio);
                view.setLayoutParams(layoutParams);
                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onAssignItemClick(myViewHolder.getAdapterPosition());
                    }
                });
            }
        };
    }

    private void initialView() {
        TextView textView = findViewById(R.id.titleTextView);
        textView.setText("统计信息");
        findViewById(R.id.navagationIco).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recordAdapter);

        tabLayout = findViewById(R.id.tabLayout2);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                step = 1;
                currentTabPosition = tab.getPosition();
                changeAdapter();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void changeAdapter(){
        TabLayout.Tab tab = tabLayout.getTabAt(currentTabPosition);
        if (currentTabPosition == 0) {
            tab.setText("所有项目总览");
            recyclerView.setAdapter(recordAdapter);
        } else {
            tab.setText("分配情况总览");
            recyclerView.setAdapter(assignAdapter);
        }
    }

    private void onRecordItemClick(int position){
        String itemname = recordDetailsList.get(position).getItemName();
        Intent intent = new Intent(Infomation.this,TestBrokenLineGraph.class);
        intent.putExtra("mode",2);
        intent.putExtra("itemname",itemname);
        startActivity(intent);
//        double max = 0;
//        step = 2;
//        final String itemname = recordDetailsList.get(position).getItemName();
//        tabLayout.getTabAt(currentTabPosition).setText(itemname);
//        List<RecordDetails> list = DbManager.find(RecordDetails.class,
//                false,
//                "itemname = ?",
//                new String[]{itemname},
//                null,
//                null,
//                "month");
//        for(RecordDetails details:list){
//            max = Math.max(max,details.getAmount());
//        }
//        final double finalMax = max;
//        BaseAdapter<RecordDetails> adapter = new BaseAdapter<RecordDetails>(R.layout.info_details_item,list) {
//            @Override
//            public void bindData(BaseViewHolder baseViewHolder, RecordDetails data) {
//                baseViewHolder.setText(R.id.monthTextView,data.getMonth().format(DateUtils.getYyyyM_Formatter()));
//                baseViewHolder.setText(R.id.amountTextView,String.valueOf(data.getAmount()));
//                double ratio = data.getAmount() / finalMax;
//                View view = baseViewHolder.getView(R.id.progressView);
//                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//                int maxWidth = parentWidth - Tools.dip2px(Infomation.this,160);
//                layoutParams.width = (int) (maxWidth * ratio);
//                view.setLayoutParams(layoutParams);
//            }
//        };
//        recyclerView.setAdapter(adapter);
    }

    private void onAssignItemClick(int position){
        String personname = assignList.get(position).getPersonName();
        Intent intent = new Intent(Infomation.this,TestBrokenLineGraph.class);
        intent.putExtra("mode",1);
        intent.putExtra("personname",personname);
        startActivity(intent);
//        double max = 0;
//        step = 1;
//        tabLayout.getTabAt(currentTabPosition).setText(personname);
//        List<AssignDetails> list = DbManager.find(AssignDetails.class,
//                false,
//                "personname = ?",
//                new String[]{personname},
//                null,
//                null,
//                "month");
//        for(AssignDetails details : list){
//            max = Math.max(max,details.getAssignAmount());
//        }
//        final double finalMax = max;
//        BaseAdapter<AssignDetails> adapter = new BaseAdapter<AssignDetails>(R.layout.info_details_item,list) {
//            @Override
//            public void bindData(BaseViewHolder baseViewHolder, AssignDetails data) {
//                baseViewHolder.setText(R.id.monthTextView,data.getMonth().format(DateUtils.getYyyyM_Formatter()));
//                baseViewHolder.setText(R.id.amountTextView,String.valueOf(data.getAssignAmount()));
//                double ratio = data.getAssignAmount() / finalMax;
//                View view = baseViewHolder.getView(R.id.progressView);
//                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//                int maxWidth = parentWidth - Tools.dip2px(Infomation.this,160);
//                layoutParams.width = (int) (maxWidth * ratio);
//                view.setLayoutParams(layoutParams);
//            }
//        };
//        recyclerView.setAdapter(adapter);
    }
}
