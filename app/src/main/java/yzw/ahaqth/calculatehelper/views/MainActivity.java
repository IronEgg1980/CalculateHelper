package yzw.ahaqth.calculatehelper.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.tools.DbHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DbHelper.initial(this);// 数据库初始化
        setContentView(R.layout.activity_main);
        findViewById(R.id.openInputActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,WorkMainActivity.class));
            }
        });
        findViewById(R.id.openHistoryActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,HistoryActivity.class));
            }
        });
        findViewById(R.id.openRemainActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ShowRemain.class));
            }
        });
        findViewById(R.id.openSetupActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        DbHelper.onDestory();
        super.onDestroy();
    }
}
