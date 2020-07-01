package yzw.ahaqth.calculatehelper.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;

import yzw.ahaqth.calculatehelper.moduls.AssignDetails;
import yzw.ahaqth.calculatehelper.moduls.Item;
import yzw.ahaqth.calculatehelper.moduls.Person;
import yzw.ahaqth.calculatehelper.moduls.RecordDetails;
import yzw.ahaqth.calculatehelper.moduls.Remain;
import yzw.ahaqth.calculatehelper.moduls.RemainDetails;
import yzw.ahaqth.calculatehelper.views.interfaces.DataMode;

public final class DbHelper extends SQLiteOpenHelper {
    private static final String dbName = "calculatehelper.db"; // 数据库名称
    private static final int version = 3; // 数据库版本号

    private static DbHelper dbHelper = null;

    public static void onDestory() {
        dbHelper.close();
        dbHelper = null;
    }

    public static SQLiteDatabase getWriteDB() {
        return dbHelper.getWritableDatabase();
    }

    public static SQLiteDatabase getReadDB() {
        return dbHelper.getReadableDatabase();
    }

    public static void initial(Context context) {
        if (dbHelper == null)
            dbHelper = new DbHelper(context, dbName, null, version);
    }

    private DbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(getCreateTableSql(Person.class));
        db.execSQL(getCreateTableSql(Item.class));
        db.execSQL(getCreateTableSql(Remain.class));
        db.execSQL(getCreateTableSql(RecordDetails.class));
        db.execSQL(getCreateTableSql(AssignDetails.class));
        db.execSQL(getCreateTableSql(RemainDetails.class));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.execSQL("ALTER TABLE remaindetails ADD variablenote TEXT default '***分配结余***'");
                db.execSQL("ALTER TABLE assigndetails ADD note TEXT");
            case 2:
            case 3:
        }
    }


    private String getCreateTableSql(Class clazz) {
        String name = clazz.getName();
        int index = name.lastIndexOf(".") + 1;
        String tableName = name.substring(index).toLowerCase();

        StringBuilder builder = new StringBuilder("CREATE TABLE ").append(tableName)
                .append("(id INTEGER PRIMARY KEY AUTOINCREMENT,");

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isPrivate(field.getModifiers()) || Modifier.isProtected(field.getModifiers())) {
                field.setAccessible(true);
                String columnName = field.getName().toLowerCase();
                builder.append(columnName).append(" ");
                Type type = field.getType();
                if (type == Long.class || type == Long.TYPE
                        || type == Integer.class || type == Integer.TYPE
                        || type == Boolean.class || type == Boolean.TYPE
                        || type == LocalDate.class || type == LocalDateTime.class
                        || type == DataMode.class) {
                    builder.append("INTEGER,");
                } else if (type == String.class) {
                    builder.append("TEXT,");
                } else if (type == Float.class || type == Float.TYPE || type == Double.class || type == Double.TYPE) {
                    builder.append("REAL,");
                }
            }
        }
        return builder.substring(0, builder.length() - 1) + ")";
    }
}
