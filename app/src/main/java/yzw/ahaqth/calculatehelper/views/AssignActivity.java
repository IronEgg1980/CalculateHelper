package yzw.ahaqth.calculatehelper.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import yzw.ahaqth.calculatehelper.R;

public class AssignActivity extends AppCompatActivity {
    private final String TAG = "殷宗旺";
    private final String[] tabsText = {"金额分配", "余额分配", "查看结果"};
    private final String POSITION_FLAG = "currentposition", TIME_FLAG = "recordtime", ITEMINDEX = "itemindex", ISASSIGNED = "isassigned";
    private TabLayout tabLayout;
    private LocalDateTime recordTime;
    private Fragment amountFragment;
//    private int currentPosition = -1;

    public int clickItemIndex = -1;// 点击的项目位置
    public boolean isAssigned = false;// 是否完成分配

    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_common);
        long l = getIntent().getLongExtra("recordtime", LocalDate.now().toEpochDay());
        recordTime = LocalDateTime.ofEpochSecond(l, 0, ZoneOffset.ofHours(8));
        amountFragment = new AssignAmountFragment();
        initialTab();
        initialView();

        Log.d(TAG, "onCreate: " + l);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        recordTime = LocalDateTime.ofEpochSecond(savedInstanceState.getLong(TIME_FLAG), 0, ZoneOffset.ofHours(8));
//        currentPosition = savedInstanceState.getInt(POSITION_FLAG);
        clickItemIndex = savedInstanceState.getInt(ITEMINDEX);
        isAssigned = savedInstanceState.getBoolean(ISASSIGNED);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(TIME_FLAG, recordTime.toEpochSecond(ZoneOffset.ofHours(8)));
//        outState.putInt(POSITION_FLAG, currentPosition);
        outState.putInt(ITEMINDEX, clickItemIndex);
        outState.putBoolean(ISASSIGNED, isAssigned);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        changeToTab1();
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    private void initialView() {
        TextView title = findViewById(R.id.titleTextView);
        title.setText("金额分配");
        findViewById(R.id.navagationIco).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initialTab() {
        tabLayout = findViewById(R.id.tabLayout);
        for (String s : tabsText) {
            TabLayout.Tab tab1 = tabLayout.newTab();
            tab1.setText(s);
            tabLayout.addTab(tab1);
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        changeToTab1();
                        break;
                    case 1:
                        changeToTab2();
                        break;
                    case 2:
                        changeToTab3();
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

    public void changeToTab1() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (amountFragment.isAdded())
            fragmentTransaction.show(amountFragment);
        else {
            fragmentTransaction.replace(R.id.fragmentContainer, amountFragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    public void showAssignFragment(double totalAmount, long month) {
        AssignFragment assignFragment = AssignFragment.newInstance(totalAmount, month);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(amountFragment);
        ft.add(R.id.fragmentContainer, assignFragment, "assignFragment");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void closeAssignFragment() {
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().beginTransaction().show(amountFragment).commit();
    }

    public void changeToTab2() {

    }

    public void changeToTab3() {

    }
}