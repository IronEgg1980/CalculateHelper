package yzw.ahaqth.calculatehelper.moduls;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import yzw.ahaqth.calculatehelper.tools.DbHelper;
import yzw.ahaqth.calculatehelper.tools.DbManager;

public abstract class BaseModul {
    protected int id;

    public int getId() {
        return id;
    }

    public void save() {
        SQLiteDatabase database = DbHelper.getWriteDB();
        ContentValues contentValues = DbManager.modul2ContentValues(this.getClass(),this);
        if (contentValues != null) {
            database.insert(this.getClass().getSimpleName().toLowerCase(), null, contentValues);
        }
        database.close();
    }

    public void dele(){
        SQLiteDatabase database = DbHelper.getWriteDB();
        database.delete(this.getClass().getSimpleName().toLowerCase(), "id = ?", new String[]{String.valueOf(this.id)});
        database.close();
    }

    public void update(){
        ContentValues contentValues = DbManager.modul2ContentValues(this.getClass(),this);
        SQLiteDatabase database = DbHelper.getWriteDB();
        database.update(this.getClass().getSimpleName().toLowerCase(),contentValues, "id = ?", new String[]{String.valueOf(this.id)});
        database.close();
    }

    public boolean isSaved(){
        return this.id > 0;
    }
}
