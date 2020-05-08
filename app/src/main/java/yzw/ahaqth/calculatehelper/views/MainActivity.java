package yzw.ahaqth.calculatehelper.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.manager.RecordDetailsDbManager;
import yzw.ahaqth.calculatehelper.moduls.RecordDetails;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.views.adapters.BaseAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.BaseViewHolder;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;
import yzw.ahaqth.calculatehelper.views.interfaces.DataMode;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class MainActivity extends AppCompatActivity {
    private String TAG = "殷宗旺";
    private List<RecordDetails> dataList;
    private BaseAdapter<RecordDetails> adapter;
    private RecyclerView recyclerView;
    private DrawerLayout drawerLayout;
    private LocalDateTime recordTime;
    private RecordDetailsDbManager dbManager;
    private boolean hasUnsignedData = false;
    private TextView recordTimeTextView;

    private void initial() {
        dbManager = new RecordDetailsDbManager(this);
        dataList = dbManager.find("datamode = ?", String.valueOf(DataMode.UNASSIGNED.ordinal()));
        if (dataList.isEmpty()) {
            this.recordTime = LocalDateTime.now();
        } else {
            this.recordTime = dataList.get(0).getRecordTime();
            this.hasUnsignedData = true;
        }
        dataList.clear();
        adapter = new BaseAdapter<RecordDetails>(R.layout.recorddetails_item_layout, dataList) {
            @Override
            public void bindData(BaseViewHolder baseViewHolder, RecordDetails data) {
                baseViewHolder.setText(R.id.month_textview,"月份："+ data.getMonth().format(DateUtils.getYyyyM_Formatter()));
                baseViewHolder.setText(R.id.itemname_textview, "项目："+data.getItemName());
                baseViewHolder.setText(R.id.amount_textview,"金额："+ String.valueOf(data.getAmount()));
                baseViewHolder.setText(R.id.datamode_textview, "状态："+data.getDataMode().getDescribe());
            }
        };
    }

    private void readData() {
        String s = "当前记录时间：" + recordTime.format(DateUtils.getYyyyMdHHmmss_Formatter());
        recordTimeTextView.setText(s);
        dataList.clear();
//        dataList.addAll(dbManager.find(recordTime));
        dataList.addAll(dbManager.findAll());
        adapter.notifyDataSetChanged();
    }

    private void initialView() {
        drawerLayout = findViewById(R.id.drawerLayout);
        recordTimeTextView = findViewById(R.id.recordtime_textview);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        findViewById(R.id.menu_input).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                Intent intent = new Intent(MainActivity.this,InputActivity.class);
                intent.putExtra("recordtime",recordTime.toEpochSecond(ZoneOffset.ofHours(8)));
                startActivity(intent);
            }
        });
        findViewById(R.id.menu_assign).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                startActivity(new Intent(MainActivity.this, AssignActivity.class));
            }
        });
        findViewById(R.id.menu_person_manage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                startActivity(new Intent(MainActivity.this, PersonManageActivity.class));
            }
        });
        findViewById(R.id.menu_item_manage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                startActivity(new Intent(MainActivity.this, ItemManageActivity.class));
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(null);
//        new Test(this).test();
        initial();
        initialView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasUnsignedData)
            showDialog();
        else
            readData();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        recordTime = LocalDateTime.ofEpochSecond(savedInstanceState.getLong("recordtime"),0,ZoneOffset.ofHours(8));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong("recordtime",recordTime.toEpochSecond(ZoneOffset.ofHours(8)));
        super.onSaveInstanceState(outState);
    }

    private void showDialog() {
        DialogFactory confirmDialog = DialogFactory.getConfirmDialog("存在未分配的记录，是否继续上次的操作？\n点击【确定】继续，【取消】重新开始");
        confirmDialog.setDialogCallback(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                if (!confirmFlag) {
                    dbManager.dele("datamode = ?", String.valueOf(DataMode.UNASSIGNED.ordinal()));
                    recordTime = LocalDateTime.now();
                }
                readData();
                hasUnsignedData = false;
            }
        });
        confirmDialog.show(getSupportFragmentManager(),"dialog");
    }
}
