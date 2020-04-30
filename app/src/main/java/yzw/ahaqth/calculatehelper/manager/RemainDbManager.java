package yzw.ahaqth.calculatehelper.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import yzw.ahaqth.calculatehelper.moduls.Remain;

public class RemainDbManager extends DbManager<Remain> {
    public RemainDbManager(Context context) {
        super(context, Remain.class);
    }

    @Override
    public void save(Remain remain) {
        this.saveOrUpdate(remain);
    }

    @Override
    public int save(List<Remain> list) {
        return 0;
    }


    @Override
    public void dele(String whereClause, String... conditions) {

    }

    @Override
    public void update(Remain modified) {
        this.saveOrUpdate(modified);
    }

    @Override
    public void update(Remain newT, String whereClause, String... conditions) {
    }

    @Override
    public void saveOrUpdate(Remain remain) {
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
    public boolean isExist(Remain remain) {
        return (!findAll().isEmpty());
    }

    public boolean isExist() {
        return (!findAll().isEmpty());
    }
}
