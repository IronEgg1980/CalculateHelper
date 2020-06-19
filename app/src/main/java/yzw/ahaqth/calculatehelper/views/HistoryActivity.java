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
    public static int SCREEN_WIDTH, SCREEN_HEIGHT;
    private RecyclerView recyclerView;
    private MyAdapter<Record> recordAdapter;
    private List<Record> list;
    private View slideMenu, menuToggle;
    private AnimatorSet showAnimatorSet, hideAnimatorSet;
    private Handler mHadler;
    private LoadingDialog loadingDialog,restoreLoading;
    private String TAG = "殷宗旺";

    private void initial() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        SCREEN_WIDTH = dm.widthPixels;
        SCREEN_HEIGHT = dm.heightPixels;

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
        mHadler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(loadingDialog.isVisible())
                    loadingDialog.dismiss();
                if(restoreLoading.isVisible())
                    restoreLoading.dismiss();
                String info = "";
                if (msg.what == 0x01) {
                    info = "备份成功";
                }else if(msg.what == 0x03) {
                    info  = "恢复成功";
                    read();
                }else {
                    Bundle bundle = msg.getData();
                    info = "备份失败！原因为：\n" + bundle.getString("error");
                }
                ToastFactory.showCenterToast(HistoryActivity.this, info);
                return true;
            }
        });
    }

    private void initialView() {
        TextView titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText("首页 - 历史记录");
        findViewById(R.id.navagationIco).setVisibility(View.GONE);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(recordAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState != RecyclerView.SCROLL_STATE_IDLE && slideMenu.getVisibility() == View.VISIBLE) {
                    hideMenu();
                }
            }
        });
        findViewById(R.id.addButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add();
            }
        });
        findViewById(R.id.personManageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                personManage();
            }
        });
        findViewById(R.id.itemManageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemManage();
            }
        });
        findViewById(R.id.infomationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HistoryActivity.this, Infomation.class));
//                startActivity(new Intent(HistoryActivity.this,TestBrokenLineGraph.class));
            }
        });
        findViewById(R.id.backupButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMenu();
                requestPermission();
            }
        });
        findViewById(R.id.restoreButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMenu();
                showBackupFileList();
            }
        });
        findViewById(R.id.remainButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMenu();
                startActivity(new Intent(HistoryActivity.this, ShowRemain.class));
            }
        });
        slideMenu = findViewById(R.id.slideMenu);
        menuToggle = findViewById(R.id.menuToggle);
        menuToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slideMenu.getVisibility() == View.INVISIBLE)
                    showMenu();
                else
                    hideMenu();
            }
        });
        loadingDialog = LoadingDialog.getInstance("正在备份...");
        restoreLoading = LoadingDialog.getInstance("正在恢复数据...");
    }

    private void showMenu() {
        if (showAnimatorSet == null) {
            ObjectAnimator showAnimator = ObjectAnimator.ofFloat(slideMenu, "translationX", -slideMenu.getWidth(), 0);
            showAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    slideMenu.setVisibility(View.VISIBLE);
                    super.onAnimationStart(animation);
                }
            });

            ObjectAnimator toggle2Right = ObjectAnimator.ofFloat(menuToggle, "translationX", -slideMenu.getWidth(), 0);
            showAnimatorSet = new AnimatorSet();
            showAnimatorSet.playTogether(showAnimator, toggle2Right);
            showAnimatorSet.setDuration(200);
        }
        if (slideMenu.getVisibility() != View.VISIBLE)
            showAnimatorSet.start();
    }

    private void hideMenu() {
        if (hideAnimatorSet == null) {
            ObjectAnimator toggle2Left = ObjectAnimator.ofFloat(menuToggle, "translationX", 0, -slideMenu.getWidth());

            ObjectAnimator hideAnimator = ObjectAnimator.ofFloat(slideMenu, "translationX", 0, -slideMenu.getWidth());
            hideAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    slideMenu.setVisibility(View.INVISIBLE);
                }
            });

            hideAnimatorSet = new AnimatorSet();
            hideAnimatorSet.setDuration(200);
            hideAnimatorSet.playTogether(hideAnimator, toggle2Left);
        }
        if (slideMenu.getVisibility() == View.VISIBLE)
            hideAnimatorSet.start();
    }

    private void edit(final Record record) {
        Intent intent = new Intent(HistoryActivity.this, WorkMainActivity.class);
        intent.putExtra("recordtime", record.getRecordTime().toEpochSecond(ZoneOffset.ofHours(8)));
        startActivity(intent);
    }

    private void add() {
        Intent intent = new Intent(HistoryActivity.this, WorkMainActivity.class);
        startActivity(intent);
    }

    private void read() {
        list.clear();
        list.addAll(DbManager.getRecordList());
        recordAdapter.notifyDataSetChanged();
    }

    private void personManage() {
        startActivity(new Intent(HistoryActivity.this, PersonManageActivity.class));
    }

    private void itemManage() {
        startActivity(new Intent(HistoryActivity.this, ItemManageActivity.class));
    }

    private void requestPermission() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (ContextCompat.checkSelfPermission(HistoryActivity.this, Manifest.permission_group.STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(HistoryActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        0x11);
            } else {
                backup();
            }
        }
    }


    private void backup() {
        loadingDialog.show(getSupportFragmentManager(), "backup");
        final File backupFile = new File(Environment.getExternalStorageDirectory(), LocalDateTime.now().format(DateUtils.getYyyyMd_Formatter()) + ".bak");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!backupFile.exists())
                        backupFile.createNewFile();
                    JSON.writeJSONString(new FileWriter(backupFile), new BackupEntity());
                    Thread.sleep(1000);
                    mHadler.sendEmptyMessage(0x01);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    Message msg = mHadler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("error", e.getLocalizedMessage());
                    msg.what = 0x02;
                    msg.setData(bundle);
                    mHadler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void showBackupFileList() {
        List<String> fileNameList = new ArrayList<>();
        for (File file : Environment.getExternalStorageDirectory().listFiles()){
            if(file.isFile()&&file.getName().endsWith(".bak"))
                fileNameList.add(file.getName());
        }
        if(fileNameList.isEmpty()){
            ToastFactory.showCenterToast(HistoryActivity.this,"未找到备份文件...");
            return;
        }
        SelectListPopWindow selectListPopWindow = new SelectListPopWindow(HistoryActivity.this, fileNameList);
        selectListPopWindow.setOnItemSelected(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                if (confirmFlag) {
                    restore((String) values[0]);
                }
            }
        }).show();
    }

    private void restore(final String fileName) {
        restoreLoading.show(getSupportFragmentManager(),"restore");
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(new File(Environment.getExternalStorageDirectory(), fileName));
                    BackupEntity backupEntity = JSON.parseObject(inputStream, StandardCharsets.UTF_8,BackupEntity.class);
                    DbManager.saveRestoreData(backupEntity);
                    Thread.sleep(1000);
                    mHadler.sendEmptyMessage(0x03);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    Message msg = mHadler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("error", e.getLocalizedMessage());
                    msg.what = 0x02;
                    msg.setData(bundle);
                    mHadler.sendMessage(msg);
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0x11) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                backup();
            } else {
                ToastFactory.showCenterToast(HistoryActivity.this, "您拒绝了授予使用外部存储权限");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        DbHelper.initial(this);// 数据库初始化

        initial();
        initialView();
    }

    @Override
    protected void onDestroy() {
        DbHelper.onDestory();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        read();
        slideMenu.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideMenu();
            }
        }, 500);
    }
}
