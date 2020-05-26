package yzw.ahaqth.calculatehelper.tools;

import android.content.Context;

import com.alibaba.fastjson.JSON;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import yzw.ahaqth.calculatehelper.moduls.BackupEntity;

public abstract class Tools {
    public static int dip2px(Context context, int dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    public static int px2dip(Context context, int px) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5);
    }

    public static void backup(File backupFile) throws IOException {

    }
}
