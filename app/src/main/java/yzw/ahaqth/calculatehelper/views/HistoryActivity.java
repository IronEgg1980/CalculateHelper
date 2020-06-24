package yzw.ahaqth.calculatehelper.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.BackupEntity;
import yzw.ahaqth.calculatehelper.moduls.Record;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.tools.DbHelper;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;
import yzw.ahaqth.calculatehelper.views.dialogs.LoadingDialog;
import yzw.ahaqth.calculatehelper.views.dialogs.SelectListPopWindow;
import yzw.ahaqth.calculatehelper.views.dialogs.ToastFactory;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyAdapter<Record> recordAdapter;
    private List<Record> list;

    private void initial() {
        list = new ArrayList<>();
        recordAdapter = new MyAdapter<Record>(list) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, final Record data) {
                myViewHolder.setText(R.id.recordtime_textview, "记录时间：" + data.getRecordTime().format(DateUtils.getYyyyMdHHmmss_Formatter()));
                myViewHolder.setText(R.id.amount_textview, "总金额：" + data.getTotalAmount());
                myViewHolder.setText(R.id.person_count_textview, "分配人数：" + data.getPersonCount());
                final SwipeMenuLayout swipeMenuLayout = myViewHolder.getView(R.id.swipeMenuLayout);
                myViewHolder.getView(R.id.swipe_menu_del).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        swipeMenuLayout.smoothClose();
                        DialogFactory.getConfirmDialog("是否删除该记录？")
                                .setDialogCallback(new DialogCallback() {
                                    @Override
                                    public void onDismiss(boolean confirmFlag, Object... values) {
                                        if (confirmFlag) {
                                            DbManager.deleHistory(data.getRecordTime());
                                            list.remove(myViewHolder.getAdapterPosition());
                                            recordAdapter.notifyItemRemoved(myViewHolder.getAdapterPosition());
                                        }
                                    }
                                })
                                .show(getSupportFragmentManager(), "confirm");

                    }
                });
                myViewHolder.getView(R.id.swipe_menu_edit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        edit(data);
                    }
                });
                myViewHolder.getView(R.id.root).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HistoryActivity.this, ShowRecordDetailsActivity.class);
                        intent.putExtra("recordtime", data.getRecordTime().toEpochSecond(ZoneOffset.ofHours(8)));
                        startActivity(intent);
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.record_item_layout;
            }
        };
    }

    private void initialView() {
        TextView titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText("历史记录");
        findViewById(R.id.navagationIco).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(recordAdapter);
    }


    private void edit(final Record record) {
        Intent intent = new Intent(HistoryActivity.this, WorkMainActivity.class);
        intent.putExtra("recordtime", record.getRecordTime().toEpochSecond(ZoneOffset.ofHours(8)));
        startActivity(intent);
    }

    private void read() {
        list.clear();
        list.addAll(DbManager.getRecordList());
        recordAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initial();
        initialView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        read();
    }
}
