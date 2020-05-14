package yzw.ahaqth.calculatehelper.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.RecordDetailsGroupByItem;
import yzw.ahaqth.calculatehelper.moduls.Remain;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.tools.DbHelper;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.BaseAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.BaseViewHolder;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;
import yzw.ahaqth.calculatehelper.views.dialogs.HistoryActivity;
import yzw.ahaqth.calculatehelper.views.dialogs.ToastFactory;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class MainActivity extends AppCompatActivity {
    private String TAG = "殷宗旺";
    private List<RecordDetailsGroupByItem> dataList;
    private BaseAdapter<RecordDetailsGroupByItem> adapter;
    private RecyclerView recyclerView;
    private DrawerLayout drawerLayout;
    private LocalDateTime recordTime;
    private boolean hasUnsignedData = false,jumpFlag = true;
    private TextView recordTimeTextView;

    private void initial() {
        this.recordTime = LocalDateTime.now();
        dataList = DbManager.getUnassignedRecordGroupByItem();
        if (!dataList.isEmpty()) {
            this.recordTime = dataList.get(0).getRecordTime();
            this.hasUnsignedData = true;
            this.jumpFlag = false;
        }
        adapter = new BaseAdapter<RecordDetailsGroupByItem>(R.layout.recordgroupbyitem_item_layout, dataList) {
            @Override
            public void bindData(final BaseViewHolder baseViewHolder, final RecordDetailsGroupByItem data) {
                baseViewHolder.setText(R.id.itemname_textview,data.getItemName());
                baseViewHolder.setText(R.id.amount_textview,"总金额："+ data.getTotalAmount());
                baseViewHolder.setText(R.id.note_textview, data.getMonthNote());
                final SwipeMenuLayout menuLayout = baseViewHolder.getView(R.id.swipeMenuLayout);
                baseViewHolder.getView(R.id.swipe_menu_del).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        menuLayout.smoothClose();
                        DbManager.deleInputRecord(data);
                        readData();
                    }
                });
            }
        };
    }

    private void readData() {
        String s = "当前记录时间：" + recordTime.format(DateUtils.getYyyyMdHHmmss_Formatter());
        recordTimeTextView.setText(s);
        dataList.clear();
        dataList.addAll(DbManager.getRecordGroupByItem(recordTime));
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
                jumpToInput();
            }
        });
        findViewById(R.id.menu_assign).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();Intent intent = new Intent(MainActivity.this,AssignActivity.class);
                intent.putExtra("recordtime",recordTime.toEpochSecond(ZoneOffset.ofHours(8)));
                startActivity(intent);
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
        findViewById(R.id.menu_remain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                showRemain((TextView) v);
            }
        });
        findViewById(R.id.menu_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            }
        });
    }

    private void jumpToInput(){
        Intent intent = new Intent(MainActivity.this,InputActivity.class);
        intent.putExtra("recordtime",recordTime.toEpochSecond(ZoneOffset.ofHours(8)));
        startActivity(intent);
    }

    private void showRemain(TextView textView){
        Remain remain = DbManager.findFirst(Remain.class);
        double remainValue = remain!=null?remain.getAmount():0;
        String s = "查询余额（"+remainValue+"）";
        textView.setText(s);
        ToastFactory.showCenterToast(this,String.valueOf(remainValue));
    }

    @Override
    protected void onDestroy() {
        DbHelper.onDestory();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DbHelper.initial(this);// 数据库初始化

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
        if (hasUnsignedData) {
            showDialog();
        }else if(jumpFlag){
            jumpFlag = false;
            jumpToInput();
        }else {
            readData();
        }
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
        DialogFactory confirmDialog = DialogFactory.getConfirmDialog("存在未分配的记录，是否清除数据重新输入？");
        confirmDialog.setDialogCallback(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                hasUnsignedData = false;
                if (confirmFlag) {
//                    DbManager.deleAll(RecordDetails.class,"datamode = ?", String.valueOf(DataMode.UNASSIGNED.ordinal()));
                    DbManager.clearOldInputData(recordTime);
                    recordTime = LocalDateTime.now();
                    jumpToInput();
                } else {
                    readData();
                }
            }
        });
        confirmDialog.show(getSupportFragmentManager(),"dialog");
    }
}
