package yzw.ahaqth.calculatehelper.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.RecordDetailsGroupByItem;
import yzw.ahaqth.calculatehelper.moduls.Remain;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.dialogs.ToastFactory;

public class WorkMainActivity extends AppCompatActivity {
    private String TAG = "殷宗旺";
    private final int HISTORY_REQUESTCODE = 0x01;
    private List<RecordDetailsGroupByItem> dataList;
    private MyAdapter<RecordDetailsGroupByItem> adapter;
    private RecyclerView recyclerView;
    private LocalDateTime recordTime;
    private TextView recordTimeTextView, titleTextView;
    private View slideMenu,menuToggle;
    private AnimatorSet showAnimatorSet,hideAnimatorSet;
    private boolean hasData = false;

    private void initial() {
        dataList = new ArrayList<>();
        adapter = new MyAdapter<RecordDetailsGroupByItem>(dataList) {
            @Override
            public int getLayoutId(int position) {
                return R.layout.recordgroupbyitem_item_layout;
            }

            @Override
            public void bindData(final MyViewHolder myViewHolder, final RecordDetailsGroupByItem data) {
                myViewHolder.setText(R.id.itemname_textview, data.getItemName());
                myViewHolder.setText(R.id.amount_textview, "总金额：" + data.getTotalAmount());
                myViewHolder.setText(R.id.note_textview, data.getMonthNote());
                final SwipeMenuLayout menuLayout = myViewHolder.getView(R.id.swipeMenuLayout);
                myViewHolder.getView(R.id.swipe_menu_del).setOnClickListener(new View.OnClickListener() {
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

    private void showMenu(){
        if(showAnimatorSet == null){
            ObjectAnimator showAnimator = ObjectAnimator.ofFloat(slideMenu,"translationX",-slideMenu.getWidth(),0);
            showAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    slideMenu.setVisibility(View.VISIBLE);
                    super.onAnimationStart(animation);
                }
            });

            ObjectAnimator toggle2Right = ObjectAnimator.ofFloat(menuToggle,"translationX",-slideMenu.getWidth(),0);
            showAnimatorSet = new AnimatorSet();
            showAnimatorSet.playTogether(showAnimator,toggle2Right);
            showAnimatorSet.setDuration(200);
        }
        if(slideMenu.getVisibility() != View.VISIBLE)
            showAnimatorSet.start();
    }

    private void hideMenu(){
        if(hideAnimatorSet == null){
            ObjectAnimator toggle2Left = ObjectAnimator.ofFloat(menuToggle,"translationX",0,-slideMenu.getWidth());

            ObjectAnimator hideAnimator = ObjectAnimator.ofFloat(slideMenu,"translationX",0,-slideMenu.getWidth());
            hideAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    slideMenu.setVisibility(View.INVISIBLE);
                }
            });

            hideAnimatorSet = new AnimatorSet();
            hideAnimatorSet.setDuration(200);
            hideAnimatorSet.playTogether(hideAnimator,toggle2Left);
        }
        if(slideMenu.getVisibility()==View.VISIBLE)
            hideAnimatorSet.start();
    }

    private void initialView() {
        slideMenu = findViewById(R.id.slideMenu);
        menuToggle = findViewById(R.id.menuToggle);
        menuToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(slideMenu.getVisibility() == View.VISIBLE)
                    hideMenu();
                else
                    showMenu();
            }
        });
        recordTimeTextView = findViewById(R.id.recordtime_textview);
        titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText("工作区 - 记录列表");
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState != RecyclerView.SCROLL_STATE_IDLE && slideMenu.getVisibility()==View.VISIBLE)
                    hideMenu();
            }
        });

        findViewById(R.id.navagationIco).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.inputButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToInput();
            }
        });
        findViewById(R.id.assignButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WorkMainActivity.this, AssignActivity.class);
                intent.putExtra("recordtime", recordTime.toEpochSecond(ZoneOffset.ofHours(8)));
                startActivity(intent);
            }
        });
        findViewById(R.id.personManageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WorkMainActivity.this, PersonManageActivity.class));
            }
        });
        findViewById(R.id.itemManageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WorkMainActivity.this, ItemManageActivity.class));
            }
        });
        findViewById(R.id.remainAssignButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemain((TextView) v);
            }
        });
    }

    private void jumpToInput() {
        Intent intent = new Intent(WorkMainActivity.this, InputActivity.class);
        intent.putExtra("recordtime", recordTime.toEpochSecond(ZoneOffset.ofHours(8)));
        startActivity(intent);
    }

    private void showRemain(TextView textView) {
        Remain remain = DbManager.findFirst(Remain.class);
        double remainValue = remain != null ? remain.getAmount() : 0;
        ToastFactory.showCenterToast(this, String.valueOf(remainValue));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_main);
        recordTime = LocalDateTime.now();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            hasData = true;
            long l = bundle.getLong("recordtime");
            recordTime = LocalDateTime.ofEpochSecond(l, 0, ZoneOffset.ofHours(8));
        }
//        new Test(this).test();
        initial();
        initialView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(hasData) {
            readData();
            slideMenu.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideMenu();
                }
            },500);
        }else{
            hasData = true;
            jumpToInput();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        recordTime = LocalDateTime.ofEpochSecond(savedInstanceState.getLong("recordtime"), 0, ZoneOffset.ofHours(8));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong("recordtime", recordTime.toEpochSecond(ZoneOffset.ofHours(8)));
        super.onSaveInstanceState(outState);
    }

//    private void showDialog() {
//        DialogFactory confirmDialog = DialogFactory.getConfirmDialog("存在未分配的记录，是否清除数据重新输入？");
//        confirmDialog.setDialogCallback(new DialogCallback() {
//            @Override
//            public void onDismiss(boolean confirmFlag, Object... values) {
//                hasUnsignedData = false;
//                if (confirmFlag) {
////                    DbManager.deleAll(RecordDetails.class,"datamode = ?", String.valueOf(DataMode.UNASSIGNED.ordinal()));
//                    DbManager.clearOldInputData();
//                    recordTime = LocalDateTime.now();
//                    jumpToInput();
//                } else {
//                    readData();
//                }
//            }
//        });
//        confirmDialog.show(getSupportFragmentManager(),"dialog");
//    }
}
