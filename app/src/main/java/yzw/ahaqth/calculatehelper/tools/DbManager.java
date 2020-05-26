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
import yzw.ahaqth.calculatehelper.moduls.AssignGroupByPerson;
import yzw.ahaqth.calculatehelper.moduls.BaseModul;
import yzw.ahaqth.calculatehelper.moduls.Record;
import yzw.ahaqth.calculatehelper.moduls.RecordDetails;
import yzw.ahaqth.calculatehelper.moduls.RecordDetailsGroupByItem;
import yzw.ahaqth.calculatehelper.moduls.RecordDetailsGroupByMonth;
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

    public static <T extends BaseModul> int saveAll(List<T> list) {
        int i = 0;
        SQLiteDatabase database = DbHelper.getWriteDB();
        database.beginTransaction();
        try {
            for (T t : list) {
                ContentValues contentValues = modul2ContentValues(t);
                if (contentValues != null) {
                    database.insert(t.getClass().getSimpleName().toLowerCase(), null, contentValues);
                    i += 1;
                }
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "save error: " + e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
        return i;
    }

    public static <T extends BaseModul> void deleAll(Class<T> clazz, String whereClause, String... conditions) {
        SQLiteDatabase database = DbHelper.getWriteDB();
        database.delete(clazz.getSimpleName().toLowerCase(), whereClause, conditions);
    }

    public static <T extends BaseModul> void updateAll(Class<T> clazz, ContentValues contentValues, String whereClause, String... conditions) {
        SQLiteDatabase database = DbHelper.getWriteDB();
        database.update(clazz.getSimpleName().toLowerCase(), contentValues, whereClause, conditions);
    }

    public static <T extends BaseModul> void updateAll(Class<T> clazz, T newT, String whereClause, String... conditions) {
        ContentValues contentValues = modul2ContentValues(newT);
        updateAll(clazz, contentValues, whereClause, conditions);
    }

    public static <T extends BaseModul> List<T> findBySql(Class<T> clazz, String sql, String... selectionArgs) {
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
        return list;
    }

    public static <T extends BaseModul> List<T> find(Class<T> clazz, boolean distinct, String selection,
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
        return list;
    }

    public static <T extends BaseModul> List<T> find(Class<T> clazz, String selection, String... selectionArgs) {
        return find(clazz, false, selection, selectionArgs, null, null, null);
    }

    public static <T extends BaseModul> List<T> findAll(Class<T> clazz) {
        return find(clazz, null);
    }

    public static <T extends BaseModul> T findFirst(Class<T> clazz) {
        T t = null;
        SQLiteDatabase database = DbHelper.getReadDB();
        Cursor cursor = database.query(false, clazz.getSimpleName().toLowerCase(), null,
                null, null, null, null, "id", null);
        if (cursor.moveToFirst()) {
            t = cursor2Modul(clazz, cursor);
        }
        cursor.close();
        return t;
    }

    public static <T extends BaseModul> T findLast(Class<T> clazz) {
        T t = null;
        SQLiteDatabase database = DbHelper.getReadDB();
        Cursor cursor = database.query(false, clazz.getSimpleName().toLowerCase(), null,
                null, null, null, null, "id DESC", null);
        if (cursor.moveToFirst()) {
            t = cursor2Modul(clazz, cursor);
        }
        cursor.close();
        return t;
    }

    public static <T extends BaseModul> T findOne(Class<T> clazz, String selection, String... args) {
        T t = null;
        SQLiteDatabase database = DbHelper.getReadDB();
        Cursor cursor = database.query(false, clazz.getSimpleName().toLowerCase(), null,
                selection, args, null, null, "id", null);
        if (cursor.moveToFirst()) {
            t = cursor2Modul(clazz, cursor);
        }
        cursor.close();
        return t;
    }

    public static boolean isEmpty(Class clazz) {
        return findAll(clazz).isEmpty();
    }

    public static <T extends BaseModul> boolean isExist(Class<T> clazz, String selection, String... selectionArgs) {
        return !find(clazz, selection, selectionArgs).isEmpty();
    }

    public static boolean clearOldInputData(LocalDateTime recordTime) {
        boolean result = false;

        String selection = "recordtime = ? ";
        String[] args = new String[]{String.valueOf(recordTime.toEpochSecond(ZoneOffset.ofHours(8)))};

        Remain remain = findFirst(Remain.class);
        double remainValue = 0;
        if (remain != null) {
            remainValue = remain.getAmount();
            List<RemainDetails> list = find(RemainDetails.class, selection, args);
            for (RemainDetails remainDetails : list) {
                remainValue = BigDecimalHelper.minus(remainValue, remainDetails.getVariableAmount());
            }
        }

        SQLiteDatabase sqLiteDatabase = DbHelper.getWriteDB();
        sqLiteDatabase.beginTransaction();

        try {
            ContentValues contentValues1 = new ContentValues();
            contentValues1.put("amount", remainValue);
            sqLiteDatabase.update(Remain.class.getSimpleName().toLowerCase(), contentValues1, null, null);

            sqLiteDatabase.delete(AssignDetails.class.getSimpleName(), selection, args);
            sqLiteDatabase.delete(RemainDetails.class.getSimpleName(), selection, args);
            sqLiteDatabase.delete(RecordDetails.class.getSimpleName(), selection, args);

            sqLiteDatabase.setTransactionSuccessful();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "rollBack error,Message : " + e.getLocalizedMessage());
        } finally {
            sqLiteDatabase.endTransaction();
            Log.d(TAG, "saveData: success！");
        }
        return result;
    }

    public static boolean saveAssignData(List<AssignDetails> list, RemainDetails remainDetails) {
        boolean result;
        Remain remain = findFirst(Remain.class);

        String[] args = {String.valueOf(remainDetails.getRecordTime().toEpochSecond(ZoneOffset.ofHours(8))),
                String.valueOf(remainDetails.getMonth().toEpochDay())};

        SQLiteDatabase sqLiteDatabase = DbHelper.getWriteDB();
        sqLiteDatabase.beginTransaction();
        try {
            for (AssignDetails assignDetails : list) {
                ContentValues contentValues = DbManager.modul2ContentValues(assignDetails);
                if (contentValues != null) {
                    sqLiteDatabase.insert(AssignDetails.class.getSimpleName().toLowerCase(), null, contentValues);
                }
            }
            if (remain == null) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("amount", remainDetails.getVariableAmount());
                sqLiteDatabase.insert(Remain.class.getSimpleName().toLowerCase(), null, contentValues);
            } else {
                ContentValues contentValues = new ContentValues();
                contentValues.put("amount", BigDecimalHelper.add(remain.getAmount(), remainDetails.getVariableAmount()));
                sqLiteDatabase.update(Remain.class.getSimpleName().toLowerCase(), contentValues, null, null);
            }

            ContentValues contentValues1 = DbManager.modul2ContentValues(remainDetails);
            sqLiteDatabase.insert(RemainDetails.class.getSimpleName().toLowerCase(), null, contentValues1);

            ContentValues contentValues2 = new ContentValues();
            contentValues2.put("datamode", DataMode.ASSIGNED.ordinal());
            sqLiteDatabase.update(RecordDetails.class.getSimpleName(), contentValues2, "recordtime = ? and month = ?", args);

            sqLiteDatabase.setTransactionSuccessful();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
            Log.d(TAG, "saveData: error,Message : " + e.getLocalizedMessage());
        } finally {
            sqLiteDatabase.endTransaction();
            Log.d(TAG, "saveData: success！");
        }
        return result;
    }

    public static boolean saveInput(List<RecordDetails> list){
        boolean b = false;
        if(!list.isEmpty()) {
            String selection = "recordtime = ?";
            String[] args = new String[]{String.valueOf(list.get(0).getRecordTime().toEpochSecond(ZoneOffset.ofHours(8)))};

            Remain remain = findFirst(Remain.class);
            double remainValue = remain == null ? 0 : remain.getAmount();

            RemainDetails remainDetails = findOne(RemainDetails.class, selection, args);
            double remainDetailsValue = remainDetails == null ? 0 : remainDetails.getVariableAmount();

            SQLiteDatabase database = DbHelper.getWriteDB();
            database.beginTransaction();
            try {
                for (RecordDetails t : list) {
                    ContentValues contentValues = modul2ContentValues(t);
                    if (contentValues != null) {
                        database.insert(RecordDetails.class.getSimpleName().toLowerCase(), null, contentValues);
                    }
                }

                ContentValues contentValues1 = new ContentValues();
                contentValues1.put("amount", BigDecimalHelper.minus(remainValue, remainDetailsValue));
                database.update(Remain.class.getSimpleName().toLowerCase(), contentValues1, null, null);

                database.delete(AssignDetails.class.getSimpleName(), selection, args);
                database.delete(RemainDetails.class.getSimpleName(), selection, args);

                ContentValues contentValues2 = new ContentValues();
                contentValues2.put("datamode", DataMode.UNASSIGNED.ordinal());
                database.update(RecordDetails.class.getSimpleName(), contentValues2, selection, args);
                database.setTransactionSuccessful();
                b = true;
            } catch (Exception e) {
                Log.d(TAG, "save error: " + e.getLocalizedMessage());
                e.printStackTrace();
            } finally {
                database.endTransaction();
            }
        }
        return b;
    }

    public static boolean deleInputRecord(RecordDetailsGroupByItem recordDetailsGroupByItem) {
        boolean result = false;
        String selection1 = "recordtime = ?";
        String selection2 = "recordtime = ? and itemname = ?";
        String[] args1 = {String.valueOf(recordDetailsGroupByItem.getRecordTime().toEpochSecond(ZoneOffset.ofHours(8)))};
        String[] args2 = {String.valueOf(recordDetailsGroupByItem.getRecordTime().toEpochSecond(ZoneOffset.ofHours(8))),
                recordDetailsGroupByItem.getItemName()};

        Remain remain = findFirst(Remain.class);
        double remainValue = 0;
        if (remain != null) {
            remainValue = remain.getAmount();
            List<RemainDetails> list = find(RemainDetails.class, selection1, args1);
            for (RemainDetails remainDetails : list) {
                remainValue = BigDecimalHelper.minus(remainValue, remainDetails.getVariableAmount());
            }
        }

        SQLiteDatabase database = DbHelper.getWriteDB();
        database.beginTransaction();
        try {
            database.delete(RecordDetails.class.getSimpleName(), selection2, args2);
            database.delete(AssignDetails.class.getSimpleName(), selection1, args1);
            database.delete(RemainDetails.class.getSimpleName(), selection1, args1);

            ContentValues contentValues1 = new ContentValues();
            contentValues1.put("datamode", String.valueOf(DataMode.UNASSIGNED.ordinal()));
            database.update(RecordDetails.class.getSimpleName(), contentValues1, selection1, args1);

            ContentValues contentValues2 = new ContentValues();
            contentValues2.put("amount", remainValue);
            database.update(Remain.class.getSimpleName(), contentValues2, null, null);

            result = true;
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "deleInputRecord error,Message : " + e.getLocalizedMessage());
        } finally {
            database.endTransaction();
        }
        return result;
    }

    public static void deleHistory(LocalDateTime recordTime){
        String selection = "recordtime = ?";
        String[] args = {String.valueOf(recordTime.toEpochSecond(ZoneOffset.ofHours(8)))};
        SQLiteDatabase database = DbHelper.getWriteDB();
        database.beginTransaction();
        try {
            database.delete(RecordDetails.class.getSimpleName(), selection, args);
            database.delete(AssignDetails.class.getSimpleName(), selection, args);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "deleInputRecord error,Message : " + e.getLocalizedMessage());
        } finally {
            database.endTransaction();
        }
    }

    public static boolean rollBackAssign(LocalDateTime recordTime, LocalDate month) {
        boolean result = false;
        String selection = "recordtime = ? and month = ?";
        String[] args = new String[]{String.valueOf(recordTime.toEpochSecond(ZoneOffset.ofHours(8))), String.valueOf(month.toEpochDay())};

        Remain remain = findFirst(Remain.class);
        double remainValue = remain == null ? 0 : remain.getAmount();

        RemainDetails remainDetails = findOne(RemainDetails.class, selection, args);
        double remainDetailsValue = remainDetails == null ? 0 : remainDetails.getVariableAmount();

        SQLiteDatabase sqLiteDatabase = DbHelper.getWriteDB();
        sqLiteDatabase.beginTransaction();

        try {
            ContentValues contentValues1 = new ContentValues();
            contentValues1.put("amount", BigDecimalHelper.minus(remainValue, remainDetailsValue));
            sqLiteDatabase.update(Remain.class.getSimpleName().toLowerCase(), contentValues1, null, null);

            sqLiteDatabase.delete(AssignDetails.class.getSimpleName(), selection, args);
            sqLiteDatabase.delete(RemainDetails.class.getSimpleName(), selection, args);

            ContentValues contentValues2 = new ContentValues();
            contentValues2.put("datamode", DataMode.UNASSIGNED.ordinal());
            sqLiteDatabase.update(RecordDetails.class.getSimpleName(), contentValues2, selection, args);

            sqLiteDatabase.setTransactionSuccessful();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "rollBack error,Message : " + e.getLocalizedMessage());
        } finally {
            sqLiteDatabase.endTransaction();
        }
        return result;
    }

    public static int count(String tableName, String column, String selection, String... selectionArgs) {
        SQLiteDatabase database = DbHelper.getReadDB();
        Cursor cursor = database.query(true, tableName, null, selection, selectionArgs, column, null, column, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public static List<RecordDetailsGroupByItem> getUnassignedRecordGroupByItem() {
        List<RecordDetails> list = find(RecordDetails.class,
                false,
                "datamode = ?",
                new String[]{String.valueOf(DataMode.UNASSIGNED.ordinal())},
                null,
                null,
                "itemname,month");
        return getRecordGroupByItem(list);
    }

    public static List<RecordDetailsGroupByItem> getRecordGroupByItem(LocalDateTime recordTime) {
        List<RecordDetails> list = find(RecordDetails.class,
                false,
                "recordtime = ?",
                new String[]{String.valueOf(recordTime.toEpochSecond(ZoneOffset.ofHours(8)))},
                null,
                null,
                "itemname,month");
        return getRecordGroupByItem(list);
    }

    public static List<RecordDetailsGroupByItem> getInfomationList(){
        List<RecordDetails> list = find(RecordDetails.class,
                false,
                null,
                null,
                null,
                null,
                "itemname,month"
                );
        return getRecordGroupByItem(list);
    }

    public static List<AssignGroupByPerson> getAssignInformationList(){
        List<AssignDetails> list = find(AssignDetails.class,
                false,
                null,
                null,
                null,
                null,
                "personname,month");
        return getAssignGroupByPerson(list);
    }

    public static List<RecordDetailsGroupByItem> getRecordGroupByItem(List<RecordDetails> list) {
        List<RecordDetailsGroupByItem> result = new ArrayList<>();
        if (!list.isEmpty()) {
            String itemName = list.get(0).getItemName();
            double totalAmount = 0;
            LocalDateTime recordTime = list.get(0).getRecordTime();

            StringBuilder note = new StringBuilder();
            for (RecordDetails details : list) {
                if (!itemName.equals(details.getItemName())) {
                    RecordDetailsGroupByItem recordDetailsGroupByItem = new RecordDetailsGroupByItem();
                    recordDetailsGroupByItem.setRecordTime(recordTime);
                    recordDetailsGroupByItem.setItemName(itemName);
                    recordDetailsGroupByItem.setMonthNote(note.substring(1));
                    recordDetailsGroupByItem.setTotalAmount(totalAmount);
                    result.add(recordDetailsGroupByItem);

                    note.delete(0, note.length());
                    totalAmount = 0;
                }
                itemName = details.getItemName();
                recordTime = details.getRecordTime();
                totalAmount = BigDecimalHelper.add(totalAmount, details.getAmount());
                note.append("\n")
                        .append(details.getMonth().format(DateUtils.getYyyyM_Formatter()))
                        .append(" : ")
                        .append(details.getAmount())
                        .append(" (")
                        .append(details.getDataMode().getDescribe())
                        .append(")");
            }
            RecordDetailsGroupByItem last = new RecordDetailsGroupByItem();
            last.setRecordTime(recordTime);
            last.setItemName(itemName);
            last.setMonthNote(note.substring(1));
            last.setTotalAmount(totalAmount);
            result.add(last);
        }
        return result;
    }

    public static List<RecordDetailsGroupByMonth> getRecordGroupByMonth(List<RecordDetails> list) {
        List<RecordDetailsGroupByMonth> result = new ArrayList<>();
        if (!list.isEmpty()) {
            LocalDateTime recordTime = list.get(0).getRecordTime();
            LocalDate month = list.get(0).getMonth();
            double totalAmount = 0;
            DataMode dataMode = list.get(0).getDataMode();
            StringBuilder itemNote = new StringBuilder();

            for (RecordDetails details : list) {
                if (!month.isEqual(details.getMonth())) {
                    RecordDetailsGroupByMonth recordDetailsGroupByMonth = new RecordDetailsGroupByMonth();
                    recordDetailsGroupByMonth.setRecordTime(recordTime);
                    recordDetailsGroupByMonth.setMonth(month);
                    recordDetailsGroupByMonth.setDataMode(dataMode);
                    recordDetailsGroupByMonth.setTotalAmount(totalAmount);
                    recordDetailsGroupByMonth.setItemNote(itemNote.substring(1));
                    result.add(recordDetailsGroupByMonth);

                    totalAmount = 0;
                    itemNote.delete(0, itemNote.length());
                }
                month = details.getMonth();
                totalAmount = BigDecimalHelper.add(totalAmount, details.getAmount());
                dataMode = details.getDataMode();
                itemNote.append("、")
                        .append(details.getItemName())
                        .append("(")
                        .append(details.getAmount())
                        .append(")");

            }
            RecordDetailsGroupByMonth last = new RecordDetailsGroupByMonth();
            last.setRecordTime(recordTime);
            last.setMonth(month);
            last.setDataMode(dataMode);
            last.setTotalAmount(totalAmount);
            last.setItemNote(itemNote.substring(1));
            result.add(last);
        }
        return result;
    }

    public static List<RecordDetailsGroupByMonth> getRecordGroupByMonth(LocalDateTime recordTime) {
        List<RecordDetails> list = DbManager.find(RecordDetails.class,
                false,
                "recordtime = ?",
                new String[]{String.valueOf(recordTime.toEpochSecond(ZoneOffset.ofHours(8)))},
                null,
                null,
                "month,itemname");
        return getRecordGroupByMonth(list);
    }

    public static List<AssignGroupByPerson> getAssignGroupByPerson(List<AssignDetails> list) {
        List<AssignGroupByPerson> resultList = new ArrayList<>();
        if (!list.isEmpty()) {
            LocalDateTime localDateTime = list.get(0).getRecordTime();
            String personName = list.get(0).getPersonName();
            double amount = 0;
            StringBuilder monthList = new StringBuilder();
            StringBuilder offDaysNote = new StringBuilder();


            for (AssignDetails assignDetails : list) {
                if (!assignDetails.getPersonName().equals(personName)) {
                    AssignGroupByPerson assignGroupByPerson = new AssignGroupByPerson();
                    assignGroupByPerson.setRecordTime(localDateTime);
                    assignGroupByPerson.setPersonName(personName);
                    assignGroupByPerson.setMonthList(monthList.substring(1));
                    assignGroupByPerson.setAssignAmount(amount);
                    assignGroupByPerson.setOffDaysNote(offDaysNote.length() > 1 ? offDaysNote.substring(1) : "");
                    resultList.add(assignGroupByPerson);

                    amount = 0;
                    monthList.delete(0, monthList.length());
                    offDaysNote.delete(0, offDaysNote.length());
                }
                personName = assignDetails.getPersonName();
                amount = BigDecimalHelper.add(amount, assignDetails.getAssignAmount());
                monthList.append("、")
                        .append(assignDetails.getMonth().format(DateUtils.getYyyyM_Formatter()))
                        .append("（")
                        .append(assignDetails.getAssignAmount())
                        .append("）");
                if (BigDecimalHelper.compare(assignDetails.getOffDays(), 0) > 0) {
                    offDaysNote.append("、")
                            .append(assignDetails.getMonth().format(DateUtils.getYyyyM_Formatter()))
                            .append("缺勤 ")
                            .append(assignDetails.getOffDays())
                            .append(" 天");
                }
            }

            AssignGroupByPerson assignGroupByPerson = new AssignGroupByPerson();
            assignGroupByPerson.setRecordTime(localDateTime);
            assignGroupByPerson.setPersonName(personName);
            assignGroupByPerson.setMonthList(monthList.substring(1));
            assignGroupByPerson.setAssignAmount(amount);
            assignGroupByPerson.setOffDaysNote(offDaysNote.length() > 1 ? offDaysNote.substring(1) : "");
            resultList.add(assignGroupByPerson);
        }
        return resultList;
    }

    public static List<AssignGroupByPerson> getAssignGroupByPersonList(LocalDateTime localDateTime) {
        List<AssignDetails> findList = DbManager.find(AssignDetails.class,
                false,
                "recordtime = ?",
                new String[]{String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8)))},
                null,
                null,
                "personname,month");
        return getAssignGroupByPerson(findList);
    }

    public static List<Record> getRecordList(){
        List<Record> result = new ArrayList<>();
        List<RecordDetails> list = find(RecordDetails.class,
                false,
                null,
                null,
                null,
                null,
                "recordtime");
        if(!list.isEmpty()) {
            LocalDateTime localDateTime = list.get(0).getRecordTime();
            double totalAmount = 0;

            for(RecordDetails recordDetails:list){
                if(!recordDetails.getRecordTime().isEqual(localDateTime)){
                    int personCount = count(AssignDetails.class.getSimpleName().toLowerCase(),
                            "personname",
                            "recordtime = ?",String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8))));
                    Record record = new Record();
                    record.setRecordTime(localDateTime);
                    record.setTotalAmount(totalAmount);
                    record.setPersonCount(personCount);
                    result.add(record);

                    localDateTime = recordDetails.getRecordTime();
                    totalAmount = 0;
                }
                totalAmount = BigDecimalHelper.add(totalAmount,recordDetails.getAmount());
            }
            int personCount = count(AssignDetails.class.getSimpleName().toLowerCase(),
                    "personname",
                    "recordtime = ?",String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8))));
            Record lastOne = new Record();
            lastOne.setRecordTime(localDateTime);
            lastOne.setTotalAmount(totalAmount);
            lastOne.setPersonCount(personCount);
            result.add(lastOne);
        }
        return result;
    }
}
