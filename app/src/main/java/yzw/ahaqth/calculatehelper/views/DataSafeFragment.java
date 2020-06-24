package yzw.ahaqth.calculatehelper.views;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.BackupEntity;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.dialogs.LoadingDialog;
import yzw.ahaqth.calculatehelper.views.dialogs.SelectListPopWindow;
import yzw.ahaqth.calculatehelper.views.dialogs.ToastFactory;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class DataSafeFragment extends Fragment {
    private Handler handler;
    private LoadingDialog loadingDialog,restoreLoading;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loadingDialog = LoadingDialog.getInstance("正在执行备份操作，请稍后...");
        restoreLoading = LoadingDialog.getInstance("正在恢复数据，请稍后...");
        handler = new Handler(new Handler.Callback() {
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
                }else {
                    Bundle bundle = msg.getData();
                    info = "备份失败！原因为：\n" + bundle.getString("error");
                }
                ToastFactory.showNormalToast(getActivity(), info);
                return true;
            }
        });
        View view = inflater.inflate(R.layout.fragment_data_safe, container, false);
        view.findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backup();
            }
        });
        view.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBackupFileList();
            }
        });
        return view;
    }

    private void backup() {
        assert getFragmentManager() != null;
        loadingDialog.show(getFragmentManager(), "backup");
        final File backupFile = new File(Environment.getExternalStorageDirectory(), LocalDateTime.now().format(DateUtils.getYyyyMd_Formatter()) + ".bak");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!backupFile.exists())
                        backupFile.createNewFile();
                    JSON.writeJSONString(new FileWriter(backupFile), new BackupEntity());
                    Thread.sleep(1000);
                    handler.sendEmptyMessage(0x01);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    Message msg = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("error", e.getLocalizedMessage());
                    msg.what = 0x02;
                    msg.setData(bundle);
                    handler.sendMessage(msg);
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
            ToastFactory.showCenterToast(getActivity(),"未找到备份文件...");
            return;
        }
        SelectListPopWindow selectListPopWindow = new SelectListPopWindow(getActivity(), fileNameList);
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
        restoreLoading.show(getFragmentManager(), "restore");
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(new File(Environment.getExternalStorageDirectory(), fileName));
                    BackupEntity backupEntity = JSON.parseObject(inputStream, StandardCharsets.UTF_8, BackupEntity.class);
                    DbManager.saveRestoreData(backupEntity);
                    Thread.sleep(1000);
                    handler.sendEmptyMessage(0x03);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    Message msg = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("error", e.getLocalizedMessage());
                    msg.what = 0x02;
                    msg.setData(bundle);
                    handler.sendMessage(msg);
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
}
