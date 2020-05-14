package yzw.ahaqth.calculatehelper.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import yzw.ahaqth.calculatehelper.moduls.Remain;
import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;

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
    public void update(Remain newT, String whereClause, String... conditions) {
    }

    @Override
    public void saveOrUpdate(Remain remain) {
        ContentValues contentValues = modul2ContentValues(remain);
        if (contentValues != null) {
            if(isExist()) {
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

    public Remain findOne(){
        List<Remain> remains = findAll();
        if(remains.isEmpty())
            return new Remain();
        return remains.get(0);
    }

    public void addRemainValue(double value){
        Remain remain = findOne();
        remain.setAmount(BigDecimalHelper.add(remain.getAmount(),value));
        saveOrUpdate(remain);
    }

    @Override
    public boolean isExist(Remain remain) {
        return isExist();
    }

    public boolean isExist() {
        return (!findAll().isEmpty());
    }
}
