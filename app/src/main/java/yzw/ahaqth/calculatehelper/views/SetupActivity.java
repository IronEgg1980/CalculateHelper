package yzw.ahaqth.calculatehelper.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.views.dialogs.ToastFactory;

public class SetupActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        initialView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new ItemSetupFragment()).commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0x11) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showDataSafeFragment();
            } else {
                ToastFactory.showCenterToast(SetupActivity.this, "您拒绝授予使用存储权限，无法使用该功能！");
                Objects.requireNonNull(tabLayout.getTabAt(0)).select();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initialView() {
        TextView textView = findViewById(R.id.titleTextView);
        textView.setText("设置");
        findViewById(R.id.navagationIco).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        showItemSetupFragment();
                        break;
                    case 1:
                        showPersonSetupFragment();
                        break;
                    case 2:
                        requestPermission();
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

    private void showItemSetupFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new ItemSetupFragment()).commit();
    }

    private void showPersonSetupFragment(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new PersonSetupFragment()).commit();
    }

    private void showDataSafeFragment(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new DataSafeFragment()).commit();
    }

    private void requestPermission() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission_group.STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SetupActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        0x11);
            } else {
                showDataSafeFragment();
            }
        }
    }
}
