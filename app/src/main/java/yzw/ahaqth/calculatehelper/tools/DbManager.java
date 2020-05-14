package yzw.ahaqth.calculatehelper.tools;

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
import java.util.List;

import yzw.ahaqth.calculatehelper.moduls.AssignDetails;
import yzw.ahaqth.calculatehelper.moduls.BaseModul;
import yzw.ahaqth.calculatehelper.moduls.Remain;
import yzw.ahaqth.calculatehelper.moduls.RemainDetails;
import yzw.ahaqth.calculatehelper.views.interfaces.DataMode;


public abstract class DbManager {
    private static String TAG = "殷宗旺";

    private DbManager() {

    }

    public static <T extends BaseModul> Method generateGetMethod(Class<T> clazz, String fieldName) {
        String sb = "get" +
                fieldName.substring(0, 1).toUpperCase() +
                fieldName.substring(1);
        try {
            return clazz.getMethod(sb);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static <T extends BaseModul> Method generateSetMethod(Class<T> clazz, String fieldName) {
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

    public static List<Field> getDataBaseFields(Class clazz) {
        List<Field> list = new ArrayList<>();
        Field[] fields1 = clazz.getDeclaredFields();
        for (Field field : fields1) {
            if (Modifier.isPrivate(field.getModifiers()) || Modifier.isProtected(field.getModifiers())) {
                field.setAccessible(true);
                list.add(field);
            }
        }

        Class clazz2 = clazz.getSuperclass();
        Field[] fields2;
        if (clazz2 != null) {
            fields2 = clazz2.getDeclaredFields();
            for (Field field : fields2) {
                if (Modifier.isPrivate(field.getModifiers()) || Modifier.isProtected(field.getModifiers())) {
                    field.setAccessible(true);
                    list.add(field);
                }
            }
        }
        return list;
    }

    public static <T extends BaseModul> T cursor2Modul(Class<T> clazz, Cursor cursor) {
        try {
            T t = clazz.newInstance();
            for (Field field : getDataBaseFields(clazz)) {
                field.setAccessible(true);
                Type type = field.getType();

                int index = cursor.getColumnIndex(field.getName().toLowerCase());

                if ("id".equals(field.getName().toLowerCase())) {
                    field.set(t, cursor.getInt(index));
                    continue;
                }

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
                } else if (type == DataMode.class) {
                    value = DataMode.values()[cursor.getInt(index)];
                }
                field.set(t, value);
            }
            return t;
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T extends BaseModul> ContentValues modul2ContentValues(Object t) {
        ContentValues contentValues = new ContentValues();
        try {
            for (Field field : getDataBaseFields(t.getClass())) {
                if (field.getName().equals("id"))
                    continue;
                field.setAccessible(true);
                Type type = field.getType();
                String fieldName = field.getName().toLowerCase();
                if (type == Long.class || type == Long.TYPE) {
                    contentValues.put(fieldName, field.getLong(t));
                } else if (type == Integer.class || type == Integer.TYPE) {
                    contentValues.put(fieldName, field.getInt(t));
                } else if (type == Boolean.class || type == Boolean.TYPE) {
                    int i = field.getBoolean(t) ? 1 : 0;
                    contentValues.put(fieldName, i);
                } else if (type == Float.class || type == Float.TYPE) {
                    contentValues.put(fieldName, field.getFloat(t));
                } else if (type == Double.class || type == Double.TYPE) {
                    contentValues.put(fieldName, field.getDouble(t));
                } else if (type == String.class) {
                    contentValues.put(fieldName, field.get(t).toString());
                } else if (type == LocalDate.class) {
                    long localDate = ((LocalDate) field.get(t)).toEpochDay();
                    contentValues.put(fieldName, localDate);
                } else if (type == LocalDateTime.class) {
                    long localDate = ((LocalDateTime) field.get(t)).toEpochSecond(ZoneOffset.ofHours(8));
                    contentValues.put(fieldName, localDate);
                } else if (type == DataMode.class) {
                    int enumValue = ((DataMode) field.get(t)).ordinal();
                    contentValues.put(fieldName, enumValue);
                }
            }
            return contentValues;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T extends BaseModul> int saveAll(Class<T> clazz, List<T> list) {
        int i = 0;
        SQLiteDatabase database = DbHelper.getWriteDB();
        database.beginTransaction();
        try {
            for (T t : list) {
                ContentValues contentValues = modul2ContentValues(clazz, t);
                if (contentValues != null) {
                    database.insert(clazz.getSimpleName().toLowerCase(), null, contentValues);
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

    public static  <T extends BaseModul> void dele(Class<T> clazz, String whereClause, String... conditions) {
        SQLiteDatabase database = DbHelper.getWriteDB();
        database.delete(clazz.getSimpleName().toLowerCase(), whereClause, conditions);
        database.close();
    }

    public static  <T extends BaseModul> void deleAll(Class<T> clazz) {
        dele(clazz, null);
    }

    public static  <T extends BaseModul> void update(Class<T> clazz, ContentValues contentValues, String whereClause, String... conditions) {
        SQLiteDatabase database = DbHelper.getWriteDB();
        database.update(clazz.getSimpleName().toLowerCase(), contentValues, whereClause, conditions);
        database.close();
    }

    public static  <T extends BaseModul> void update(Class<T> clazz, T newT, String whereClause, String... conditions) {
        ContentValues contentValues = modul2ContentValues(clazz, newT);
        update(clazz,contentValues,whereClause,conditions);
    }

    public static  <T extends BaseModul> List<T> findBySql(Class<T> clazz, String sql, String... selectionArgs) {
        List<T> list = new ArrayList<>();
        SQLiteDatabase database = DbHelper.getReadDB();
        Cursor cursor = database.rawQuery(sql, selectionArgs);
        if (cursor.moveToFirst()) {
            do {
                T t = cursor2Modul(clazz, cursor);
                if (t != null) {
                    list.add(t);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return list;
    }

    public static  <T extends BaseModul> List<T> find(Class<T> clazz, boolean distinct, String selection,
                            String[] selectionArgs, String groupBy, String having,
                            String orderBy) {
        List<T> list = new ArrayList<>();
        SQLiteDatabase database = DbHelper.getReadDB();
        Cursor cursor = database.query(distinct, clazz.getSimpleName().toLowerCase(), null, selection, selectionArgs, groupBy, having, orderBy, null);
        if (cursor.moveToFirst()) {
            do {
                T t = cursor2Modul(clazz, cursor);
                if (t != null) {
                    list.add(t);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return list;
    }

    public static  <T extends BaseModul> List<T> find(Class<T> clazz, String selection, String... selectionArgs) {
        return find(clazz, false, selection, selectionArgs, null, null, null);
    }

    public static  <T extends BaseModul> List<T> findAll(Class<T> clazz) {
        return find(clazz, null);
    }

    public static  <T extends BaseModul> T findFirst(Class<T> clazz) {
        T t = null;
        SQLiteDatabase database = DbHelper.getReadDB();
        Cursor cursor = database.query(false, clazz.getSimpleName().toLowerCase(), null,
                null, null, null, null, "id", null);
        if (cursor.moveToFirst()) {
            t = cursor2Modul(clazz, cursor);
        }
        cursor.close();
        database.close();
        return t;
    }

    public static  <T extends BaseModul> T findLast(Class<T> clazz) {
        T t = null;
        SQLiteDatabase database = DbHelper.getReadDB();
        Cursor cursor = database.query(false, clazz.getSimpleName().toLowerCase(), null,
                null, null, null, null, "id DESC", null);
        if (cursor.moveToFirst()) {
            t = cursor2Modul(clazz, cursor);
        }
        cursor.close();
        database.close();
        return t;
    }

    public static  <T extends BaseModul> T findFirst(Class<T> clazz,String selection,String...args) {
        T t = null;
        SQLiteDatabase database = DbHelper.getReadDB();
        Cursor cursor = database.query(false, clazz.getSimpleName().toLowerCase(), null,
                selection, args, null, null, "id", null);
        if (cursor.moveToFirst()) {
            t = cursor2Modul(clazz, cursor);
        }
        cursor.close();
        database.close();
        return t;
    }

    public static  <T extends BaseModul> T findLast(Class<T> clazz,String selection,String...args) {
        T t = null;
        SQLiteDatabase database = DbHelper.getReadDB();
        Cursor cursor = database.query(false, clazz.getSimpleName().toLowerCase(), null,
                selection, args, null, null, "id DESC", null);
        if (cursor.moveToFirst()) {
            t = cursor2Modul(clazz, cursor);
        }
        cursor.close();
        database.close();
        return t;
    }

    public static  boolean isEmpty(Class clazz) {
        return findAll(clazz).isEmpty();
    }

    public static  <T extends BaseModul> boolean isSaved(Class<T> clazz, T t) {
        return !find(clazz, "id = ?", String.valueOf(t.getId())).isEmpty();
    }

    public static  <T extends BaseModul> boolean isExist(Class<T> clazz, String selection, String... selectionArgs) {
        return !find(clazz, selection, selectionArgs).isEmpty();
    }
}
