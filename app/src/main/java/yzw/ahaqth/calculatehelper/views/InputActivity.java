package yzw.ahaqth.calculatehelper.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.RecordDetails;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;
import yzw.ahaqth.calculatehelper.views.dialogs.ToastFactory;

public class InputActivity extends AppCompatActivity {
    private final String BUNDLE_FLAG = "recordtime";
    private LocalDateTime recordTime;
    private final String[] tabsText = {"输入", "查看"};
    private InputDataFragment inputDataFragment;

    public LocalDateTime getRecordTime(){
        return this.recordTime;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(BUNDLE_FLAG,recordTime.toEpochSecond(ZoneOffset.ofHours(8)));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        long time = savedInstanceState.getLong(BUNDLE_FLAG);
        this.recordTime = LocalDateTime.ofEpochSecond(time,0,ZoneOffset.ofHours(8));
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        recordTime = LocalDateTime.now();
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            long time = bundle.getLong("recordtime");
            this.recordTime = LocalDateTime.ofEpochSecond(time,0,ZoneOffset.ofHours(8));
        }
        initialTab();
        initialView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        changeToInputData();
    }

    private void initialTab(){
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        for (String s : tabsText) {
            TabLayout.Tab tab1 = tabLayout.newTab();
            tab1.setText(s);
            tabLayout.addTab(tab1);
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch ( tab.getPosition()) {
                    case 0:
                        changeToInputData();
                        break;
                    case 1:
                        showResult();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void changeToInputData(){
       getSupportFragmentManager()
               .beginTransaction()
               .replace(R.id.fragmentContainer,inputDataFragment)
               .commit();
    }

    private void showResult(){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer,ShowInputResultFragment.newInstance(recordTime.toEpochSecond(ZoneOffset.ofHours(8))))
                .commit();
    }

    public void changeToAssign(){
        if(DbManager.isExist(RecordDetails.class,"recordtime = ?",String.valueOf(recordTime.toEpochSecond(ZoneOffset.ofHours(8))))) {
            Intent intent = new Intent(InputActivity.this, AssignActivity.class);
            intent.putExtra("recordtime", recordTime.toEpochSecond(ZoneOffset.ofHours(8)));
            startActivity(intent);
            finish();
        }else{
            DialogFactory.getInfoDialog("请先输入数据后再开始分配").show(getSupportFragmentManager(),"info");
        }
    }

    private void initialView(){
        inputDataFragment = new InputDataFragment();
        TextView titleView = findViewById(R.id.titleTextView);
        titleView.setText("输入数据");
        findViewById(R.id.navagationIco).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToAssign();
            }
        });
    }
}
