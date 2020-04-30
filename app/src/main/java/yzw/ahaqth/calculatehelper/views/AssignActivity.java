package yzw.ahaqth.calculatehelper.views;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.manager.TempRecordDetailsManager;
import yzw.ahaqth.calculatehelper.moduls.RecorDetailsGroupByMonth;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.views.adapters.BaseAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.BaseViewHolder;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;

public class AssignActivity extends AppCompatActivity {
    private RecyclerView recyclerView1;
    private List<RecorDetailsGroupByMonth> recordDetailsGroupByMonthList;
    private BaseAdapter<RecorDetailsGroupByMonth> recordDetailsAdapter;
    private void initial(){
        this.recordDetailsGroupByMonthList = new TempRecordDetailsManager(this).getRecordGroupByMonthList();
        this.recordDetailsAdapter = new BaseAdapter<RecorDetailsGroupByMonth>(R.layout.assign_recorddetailslist_item,recordDetailsGroupByMonthList) {
            @Override
            public void bindData(BaseViewHolder baseViewHolder, final RecorDetailsGroupByMonth data) {
                baseViewHolder.setText(R.id.monthTextView,data.getMonth().format(DateUtils.getYyyyM_Formatter()));
                baseViewHolder.setText(R.id.amountTextView,String.valueOf(data.getTotalAmount()));
                baseViewHolder.setText(R.id.noteTextView,data.getItemNote());
                baseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFactory.getConfirmDialog(data.getItemNote()).show(getSupportFragmentManager(),"show");
                    }
                });
            }
        };
//        recordDetailsAdapter.setItemClickListener(new ItemClickListener() {
//            @Override
//            public void onClick(int position, Object... values) {
//                RecorDetailsGroupByMonth recorDetailsGroupByMonth = recordDetailsGroupByMonthList.get(position);
//                DialogFactory.getConfirmDialog(recorDetailsGroupByMonth.getItemNote()).show(getSupportFragmentManager(),"show");
//            }
//        });
    }

    private void initialView(){
        recyclerView1 = findViewById(R.id.recyclerview1);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        recyclerView1.setAdapter(recordDetailsAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("按月份分配");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        initial();
        initialView();

    }
}
