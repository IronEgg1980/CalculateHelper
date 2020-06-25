package yzw.ahaqth.calculatehelper.views;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.views.dialogs.ToastFactory;
import yzw.ahaqth.calculatehelper.views.interfaces.InputDataFragment;

public class WorkMainActivity extends AppCompatActivity {
    private String TAG = "殷宗旺";
    private final String BUNDLE_FLAG = "recordtime",CURRENT_FRAGMENT_FLAG = "currentfragment";
    private LocalDateTime recordTime;
    private int currentFragmentFlag ;

    public LocalDateTime getRecordTime(){
        return this.recordTime;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(BUNDLE_FLAG,recordTime.toEpochSecond(ZoneOffset.ofHours(8)));
        outState.putInt(CURRENT_FRAGMENT_FLAG,currentFragmentFlag);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        long time = savedInstanceState.getLong(BUNDLE_FLAG);
        this.recordTime = LocalDateTime.ofEpochSecond(time,0,ZoneOffset.ofHours(8));
        this.currentFragmentFlag = savedInstanceState.getInt(CURRENT_FRAGMENT_FLAG);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_main);
        recordTime = LocalDateTime.now();
        currentFragmentFlag = 1 ;
        if(savedInstanceState != null){
            long time = savedInstanceState.getLong(BUNDLE_FLAG);
            this.recordTime = LocalDateTime.ofEpochSecond(time,0,ZoneOffset.ofHours(8));
            this.currentFragmentFlag = savedInstanceState.getInt(CURRENT_FRAGMENT_FLAG);
        }
        initialView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentFragmentFlag == 1){
            changeToInputData();
        }else{
            changeToAssign();
        }
    }

    public void changeToInputData(){
        currentFragmentFlag = 1;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer,new InputDataFragment())
                .commit();
    }

    public void changeToAssign(){
        currentFragmentFlag = 2;
        ToastFactory.showCenterToast(this,"change to assign");
    }

    private void initialView(){
        TextView titleView = findViewById(R.id.titleTextView);
        titleView.setText("工作区域");
        findViewById(R.id.navagationIco).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
