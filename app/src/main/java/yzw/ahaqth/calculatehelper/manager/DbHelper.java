package yzw.ahaqth.calculatehelper.manager;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public final class DbHelper extends SQLiteOpenHelper {
    private static final String dbName = "calculatehelper.db"; // 数据库名称
    private static final int version = 1; // 数据库版本号

    private static DbHelper dbHelper = null;

    private final String createTableSql_Person = "CREATE TABLE person(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL)";
    private final String createTalbeSql_Item = "CREATE TABLE item(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL)";
    private final String createTalbeSql_Remain = "CREATE TABLE remain(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "amount REAL)";
    private final String createTalbeSql_AssignDetails = "CREATE TABLE assigndetails(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "recordtime INTEGER NOT NULL," +
            "month INTEGER," +
            "personname TEXT," +
            "offdays REAL," +
            "assignamount REAL)";
//    private final String createTalbeSql_Record = "CREATE TABLE record(" +
//            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
//            "recordtime INTEGER NOT NULL," +
//            "totalamount REAL," +
//            "personcount INTEGER)";
    private final String createTalbeSql_RecordDetails = "CREATE TABLE recorddetails(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "recordtime INTEGER NOT NULL," +
            "month INTEGER," +
            "itemname TEXT," +
            "amount REAL," +
            "datamode INTEGER)";
//    private final String createTalbeSql_TmpRecordDetails = "CREATE TABLE temprecorddetails(" +
//            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
//            "recordtime INTEGER NOT NULL," +
//            "month INTEGER," +
//            "itemname TEXT," +
//            "amount REAL," +
//            "datamode INTEGER)";
//    private final String createTalbeSql_TmpRecord = "CREATE TABLE temprecord(" +
//            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
//            "recordtime INTEGER NOT NULL," +
//            "totalamount REAL," +
//            "personcount INTEGER)";

    public static DbHelper getInstance(Context context){
        if(dbHelper == null)
            dbHelper = new DbHelper(context,dbName,null,version);
        return dbHelper;
    }

    private DbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTableSql_Person);
        db.execSQL(createTalbeSql_Item);
//        db.execSQL(createTalbeSql_Record);
        db.execSQL(createTalbeSql_Remain);
        db.execSQL(createTalbeSql_RecordDetails);
        db.execSQL(createTalbeSql_AssignDetails);
//        db.execSQL(createTalbeSql_TmpRecordDetails);
//        db.execSQL(createTalbeSql_TmpRecord);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static int count(Context context ,String tableName, String column, String selection,String...selectionArgs){
        SQLiteDatabase database = DbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = database.query(true, tableName, new String[]{column}, selection, selectionArgs, column, null, column,null);
        int count = cursor.getCount();
        cursor.close();
        database.close();
        return count;
    }
}
