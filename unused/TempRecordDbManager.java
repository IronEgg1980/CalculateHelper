package yzw.ahaqth.calculatehelper.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import yzw.ahaqth.calculatehelper.moduls.TempRecord;

public class TempRecordDbManager extends DbManager<TempRecord> {
    public TempRecordDbManager(Context context) {
        super(context, TempRecord.class);
    }

    @Override
    public void save(TempRecord remain) {
        this.saveOrUpdate(remain);
    }

    @Override
    public int save(List<TempRecord> list) {
        return 0;
    }


    @Override
    public void dele(String whereClause, String... conditions) {

    }

    @Override
    public void update(TempRecord newT, String whereClause, String... conditions) {
    }

    @Override
    public void saveOrUpdate(TempRecord remain) {
        ContentValues contentValues = modul2ContentValues(remain);
        if (contentValues != null) {
            if(isExist(remain)) {
                SQLiteDatabase database = dbHelper.getWritableDatabase();
                database.update(tableName, contentValues, null, null);
                database.close();
            }else{
                SQLiteDatabase database = dbHelper.getWritableDatabase();
                database.insert(tableName, null, contentValues);
                database.close();
            }
        }
    }

    @Override
    public boolean isExist(TempRecord remain) {
        return (!findAll().isEmpty());
    }

    public boolean isExist() {
        return (!findAll().isEmpty());
    }
}
