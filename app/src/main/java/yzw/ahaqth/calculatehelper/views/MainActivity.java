package yzw.ahaqth.calculatehelper.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.tools.DbHelper;

public class MainActivity extends AppCompatActivity {
    public static int SCREEN_WIDTH, SCREEN_HEIGHT;
    private final String TAG = "殷宗旺";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DbHelper.initial(this);// 数据库初始化
        setContentView(R.layout.activity_main);
        findViewById(R.id.openInputActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, InputActivity.class));
            }
        });
        findViewById(R.id.openHistoryActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,HistoryActivity.class));
            }
        });
        findViewById(R.id.openInfoActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Infomation.class));
            }
        });
        findViewById(R.id.openSetupActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,SetupActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        SCREEN_WIDTH = dm.widthPixels;
        SCREEN_HEIGHT = dm.heightPixels;
    }

    @Override
    protected void onDestroy() {
        DbHelper.onDestory();
        super.onDestroy();
    }
}
