package yzw.ahaqth.calculatehelper.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.Record;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

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
                myViewHolder.getView(R.id.swipe_menu_reassign).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reAssign(data);
                    }
                });
                myViewHolder.getView(R.id.swipe_menu_input).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        input(data);
                    }
                });
                myViewHolder.getView(R.id.root).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDetails(data);
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_record;
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


    private void reAssign(final Record record) {
        Intent intent = new Intent(HistoryActivity.this, AssignActivity.class);
        intent.putExtra("recordtime", record.getRecordTime().toEpochSecond(ZoneOffset.ofHours(8)));
        startActivity(intent);
    }

    private void input(Record record) {
        Intent intent = new Intent(HistoryActivity.this, InputActivity.class);
        intent.putExtra("recordtime", record.getRecordTime().toEpochSecond(ZoneOffset.ofHours(8)));
        startActivity(intent);
    }

    private void showDetails(Record record) {
        Intent intent = new Intent(HistoryActivity.this, ShowRecordDetailsActivity.class);
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
