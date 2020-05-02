package yzw.ahaqth.calculatehelper.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import yzw.ahaqth.calculatehelper.moduls.BaseModul;


public abstract class DbManager<T extends BaseModul> {
    private String TAG = "殷宗旺";
    private List<String> fieldList;
    DbHelper dbHelper;
    String tableName;
    private Class<T> clazz;

    public DbManager(Context context, Class<T> clazz) {
        this.fieldList = new ArrayList<>();
        this.dbHelper = DbHelper.getInstance(context);
        this.fieldList = new ArrayList<>();
        this.clazz = clazz;
        initial();
    }

    private void initial() {
        String name = clazz.getName();
        int index = name.lastIndexOf(".") + 1;
        this.tableName = name.substring(index).toLowerCase();

        List<Field> list = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        Class clazz2 = clazz.getSuperclass();
        if (clazz2 != null) {
            list.addAll(Arrays.asList(clazz2.getDeclaredFields()));
        }

        for (Field field : list) {
            if (Modifier.isPrivate(field.getModifiers()) || Modifier.isProtected(field.getModifiers())) {
                field.setAccessible(true);
                fieldList.add(field.getName());
            }
        }
    }

    protected Method generateGetMethod(Class<T> clazz, String fieldName) {
        String sb = "get" +
                fieldName.substring(0, 1).toUpperCase() +
                fieldName.substring(1);
        try {
            return clazz.getMethod(sb);
        } catch (Exception ignored) {
        }
        return null;
    }

    protected Method generateSetMethod(Class<T> clazz, String fieldName) {
        try {
            Class[] parameterTypes = new Class[1];
            Field field = clazz.getDeclaredField(fieldName);
            parameterTypes[0] = field.getType();
            String sb = "set" +
                    fieldName.substring(0, 1).toUpperCase() +
                    fieldName.substring(1);
            return clazz.getMethod(sb, parameterTypes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private T cursor2Modul(Cursor cursor) {
        try {
            T t = clazz.newInstance();
            for (String field : fieldList) {
                int index = cursor.getColumnIndex(field.toLowerCase());
                if ("id".equals(field)) {
                    Field field2 = Objects.requireNonNull(clazz.getSuperclass()).getDeclaredField(field);
                    field2.setAccessible(true);
                    field2.set(t, cursor.getInt(index));
                    continue;
                }
                Field field1 = clazz.getDeclaredField(field);
                field1.setAccessible(true);
                Type type = field1.getType();
                Object value = null;
                if (type == Long.class || type == Long.TYPE) {
                    value = cursor.getLong(index);
                } else if (type == Integer.class || type == Integer.TYPE) {
                    value = cursor.getInt(index);
                } else if (type == Boolean.class || type == Boolean.TYPE) {
                    value = cursor.getInt(index) != 0;
                } else if (type == Float.class || type == Float.TYPE) {
                    value = cursor.getFloat(index);
                } else if (type == Double.class || type == Double.TYPE) {
                    value = cursor.getDouble(index);
                } else if (type == String.class) {
                    value = cursor.getString(index);
                } else if (type == LocalDate.class) {
                    value = LocalDate.ofEpochDay(cursor.getLong(index));
                } else if (type == LocalDateTime.class) {
                    value = LocalDateTime.ofEpochSecond(cursor.getLong(index), 0, ZoneOffset.ofHours(8));
                }
                field1.set(t, value);
            }
            return t;
        } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    ContentValues modul2ContentValues(T t) {
        ContentValues contentValues = new ContentValues();
        try {
            for (String s : fieldList) {
                Field field1;
                if (s.equals("id"))
                    continue;
                field1 = clazz.getDeclaredField(s);
                field1.setAccessible(true);
                Type type = field1.getType();
                String field = s.toLowerCase();
                if (type == Long.class || type == Long.TYPE) {
                    contentValues.put(field, field1.getLong(t));
                } else if (type == Integer.class || type == Integer.TYPE) {
                    contentValues.put(field, field1.getInt(t));
                } else if (type == Boolean.class || type == Boolean.TYPE) {
                    int i = field1.getBoolean(t) ? 1 : 0;
                    contentValues.put(field, i);
                } else if (type == Float.class || type == Float.TYPE) {
                    contentValues.put(field, field1.getFloat(t));
                } else if (type == Double.class || type == Double.TYPE) {
                    contentValues.put(field, field1.getDouble(t));
                } else if (type == String.class) {
                    contentValues.put(field, field1.get(t).toString());
                } else if (type == LocalDate.class) {
                    long localDate = ((LocalDate) field1.get(t)).toEpochDay();
                    contentValues.put(field, localDate);
                } else if (type == LocalDateTime.class) {
                    long localDate = ((LocalDateTime) field1.get(t)).toEpochSecond(ZoneOffset.ofHours(8));
                    contentValues.put(field, localDate);
                }
            }
            return contentValues;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void save(T t) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = modul2ContentValues(t);
        if (contentValues != null) {
            database.insert(tableName, null, contentValues);
        }
        database.close();
    }

    public int save(List<T> list) {
        int i = 0;
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            for (T t : list) {
                ContentValues contentValues = modul2ContentValues(t);
                if (contentValues != null) {
                    database.insert(tableName, null, contentValues);
                    i += 1;
                }
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "save error: " + e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }
        return i;
    }

    public void dele(T t) {
        dele("id = ?",String.valueOf(t.getId()));
    }

    public void dele(String whereClause, String...conditions) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(tableName, whereClause, conditions);
        database.close();
    }

    public void deleAll() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(tableName, null, null);
        database.close();
    }

//    public void update(T modified) {
//        SQLiteDatabase database = dbHelper.getWritableDatabase();
//        ContentValues contentValues = modul2ContentValues(modified);
//        database.update(tableName, contentValues, "id = ?", new String[]{String.valueOf(modified.getId())});
//        database.close();
//    }

    public void update(T newT,String whereClause, String...conditions) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = modul2ContentValues(newT);
        database.update(tableName, contentValues, whereClause, conditions);
        database.close();
    }

    public void saveOrUpdate(T t){
        if(isExist(t)){
            update(t,"id = ?", String.valueOf(t.getId()));
        }else{
            save(t);
        }
    }

    public List<T> findBySql(String sql,String...selectionArgs){
        List<T> list = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery(sql,selectionArgs);
        if (cursor.moveToFirst()) {
            do {
                T t = cursor2Modul(cursor);
                if (t != null) {
                    list.add(t);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return list;
    }

    public List<T> find(boolean distinct, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        List<T> list = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(distinct, tableName, null, selection, selectionArgs, groupBy, having, orderBy,null);
        if (cursor.moveToFirst()) {
            do {
                T t = cursor2Modul(cursor);
                if (t != null) {
                    list.add(t);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return list;
    }

    public List<T> find(String selection, String...selectionArgs) {
        return find(false, selection, selectionArgs, null, null, null);
    }

    public List<T> findAll() {
        return find(null);
    }

    public T findOne(int id) {
        return findOne("id = ?", String.valueOf(id));
    }

    public T findOne(String selection, String...selectionArgs) {
        List<T> list = find(selection, selectionArgs);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean isExist(T t) {
        return findOne(t.getId()) != null;
    }
}
