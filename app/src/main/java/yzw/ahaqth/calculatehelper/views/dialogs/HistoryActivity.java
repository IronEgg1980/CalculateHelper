package yzw.ahaqth.calculatehelper.views.dialogs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.Record;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.BaseAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.BaseViewHolder;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BaseAdapter<Record> recordAdapter;
    private List<Record> list;
    private String TAG = "殷宗旺";

    private void initial(){
        list = DbManager.getRecordList();
        recordAdapter = new BaseAdapter<Record>(R.layout.record_item_layout,list) {
            @Override
            public void bindData(final BaseViewHolder baseViewHolder, final Record data) {
                baseViewHolder.setText(R.id.recordtime_textview,data.getRecordTime().format(DateUtils.getYyyyMdHHmmss_Formatter()));
                baseViewHolder.setText(R.id.amount_textview,String.valueOf(data.getTotalAmount()));
                baseViewHolder.setText(R.id.person_count_textview,String.valueOf(data.getPersonCount()));
                final SwipeMenuLayout swipeMenuLayout = baseViewHolder.getView(R.id.swipeMenuLayout);
                baseViewHolder.getView(R.id.swipe_menu_del).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        swipeMenuLayout.smoothClose();
                        DbManager.deleHistory(data.getRecordTime());
                        list.remove(baseViewHolder.getAdapterPosition());
                        recordAdapter.notifyItemRemoved(baseViewHolder.getAdapterPosition());
                    }
                });
            }
        };
        Log.d(TAG, "initial: list.size() = " + list.size());
    }

    private void initialView(){
        setTitle("历史记录");
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(recordAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar =  findViewById(R.id.toolbar);
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
