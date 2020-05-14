package yzw.ahaqth.calculatehelper.tools;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.moduls.AssignDetails;
import yzw.ahaqth.calculatehelper.moduls.AssignGroupByPerson;
import yzw.ahaqth.calculatehelper.moduls.RecordDetailsGroupByMonth;
import yzw.ahaqth.calculatehelper.moduls.RecordDetails;
import yzw.ahaqth.calculatehelper.moduls.RecordDetailsGroupByItem;
import yzw.ahaqth.calculatehelper.moduls.Remain;
import yzw.ahaqth.calculatehelper.moduls.RemainDetails;
import yzw.ahaqth.calculatehelper.views.interfaces.DataMode;

public class DbTools {
    private static String TAG = "殷宗旺";

    public static boolean saveTogether(List<AssignDetails> list, RemainDetails remainDetails) {
        boolean result;

        List<Remain> remains = DbManager.findAll(Remain.class);

        SQLiteDatabase sqLiteDatabase = DbHelper.getWriteDB();
        sqLiteDatabase.beginTransaction();
        try {
            for (AssignDetails assignDetails : list) {
                ContentValues contentValues = DbManager.modul2ContentValues(AssignDetails.class, assignDetails);
                if (contentValues != null) {
                    sqLiteDatabase.insert(AssignDetails.class.getName().toLowerCase(), null, contentValues);
                }
            }
            if (remains.isEmpty()) {
                Remain remain = new Remain();
                remain.setAmount(remainDetails.getVariableAmount());
                ContentValues contentValues = DbManager.modul2ContentValues(Remain.class, remain);
                sqLiteDatabase.insert(Remain.class.getName().toLowerCase(), null, contentValues);
            } else {
                Remain remain = remains.get(0);
                remain.setAmount(BigDecimalHelper.add(remain.getAmount(), remainDetails.getVariableAmount()));
                ContentValues contentValues = DbManager.modul2ContentValues(Remain.class, remain);
                sqLiteDatabase.update(Remain.class.getName().toLowerCase(), contentValues, null, null);
            }

            ContentValues contentValues = DbManager.modul2ContentValues(RemainDetails.class, remainDetails);
            sqLiteDatabase.insert(RemainDetails.class.getName().toLowerCase(), null, contentValues);

            sqLiteDatabase.setTransactionSuccessful();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
            Log.d(TAG, "saveData: error,Message : " + e.getLocalizedMessage());
        } finally {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
            Log.d(TAG, "saveData: success！");
        }
        return result;
    }

    public int count(String tableName, String column, String selection, String... selectionArgs) {
        SQLiteDatabase database = DbHelper.getReadDB();
        Cursor cursor = database.query(true, tableName, new String[]{column}, selection, selectionArgs, column, null, column, null);
        int count = cursor.getCount();
        cursor.close();
        database.close();
        return count;
    }

    public static List<RecordDetailsGroupByItem> getUnassignedRecordGroupByItem() {
//        SQLiteDatabase database = DbHelper.getReadDB();
//        List<String> items = new ArrayList<>();
//        Cursor cursor = database.query(true,
//                "recorddetails",
//                new String[]{"itemname"},
//                "datamode = ?",
//                new String[]{String.valueOf(DataMode.UNASSIGNED.ordinal())},
//                "itemname",
//                null,
//                "itemname",
//                null);
//        if (cursor != null && cursor.moveToFirst()) {
//            do {
//                String itemname = cursor.getString(cursor.getColumnIndex("itemname"));
//                items.add(itemname);
//            } while (cursor.moveToNext());
//            cursor.close();
//        }
        List<RecordDetailsGroupByItem> result = new ArrayList<>();

        List<RecordDetails> list = DbManager.find(RecordDetails.class,
                false,
                "datamode = ?",
                 new String[]{String.valueOf(DataMode.UNASSIGNED.ordinal())},
                null,
                null,
                "itemname,month");
        if (list.isEmpty())
            return result;

        String itemName = list.get(0).getItemName();
        double totalAmount = 0;
        LocalDateTime recordTime = list.get(0).getRecordTime();
        StringBuilder note = new StringBuilder("");
        for (RecordDetails details : list) {
            if (!itemName.equals(details.getItemName())) {
                RecordDetailsGroupByItem recordDetailsGroupByItem = new RecordDetailsGroupByItem();
                recordDetailsGroupByItem.setRecordTime(recordTime);
                recordDetailsGroupByItem.setItemName(itemName);
                recordDetailsGroupByItem.setMonthNote(note.substring(1));
                recordDetailsGroupByItem.setTotalAmount(totalAmount);
                result.add(recordDetailsGroupByItem);
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

        return result;
    }

    public static List<RecordDetailsGroupByMonth> getRecordGroupByMonthList(LocalDateTime recordTime) {
        List<RecordDetails> list = DbManager.find(RecordDetails.class,
                false,
                "recordtime = ?",
                new String[]{String.valueOf(recordTime.toEpochSecond(ZoneOffset.ofHours(8)))},
                null,
                null,
                "month,itemname");
        List<RecordDetailsGroupByMonth> result = new ArrayList<>();
        if (list.isEmpty())
            return result;

        LocalDate month = result.get(0).getMonth();
        double totalAmount = 0;
        DataMode dataMode = result.get(0).getDataMode();
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

        return result;
    }

    public static List<AssignGroupByPerson> getAssignGroupByPersonList(LocalDateTime localDateTime) {
        List<AssignDetails> findList = DbManager.find(AssignDetails.class,
                false,
                "recordtime = ?",
                new String[]{String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8)))},
                null,
                null,
                "personname,month");
        List<AssignGroupByPerson> resultList = new ArrayList<>();
        if (!findList.isEmpty()) {
            String personName = findList.get(0).getPersonName();
            double amount = 0;
            StringBuilder monthList = new StringBuilder();
            StringBuilder offDaysNote = new StringBuilder();


            for (AssignDetails assignDetails : findList) {
                if (!assignDetails.getPersonName().equals(personName)) {
                    AssignGroupByPerson assignGroupByPerson = new AssignGroupByPerson();
                    assignGroupByPerson.setRecordTime(localDateTime);
                    assignGroupByPerson.setPersonName(personName);
                    assignGroupByPerson.setMonthList(monthList.substring(1));
                    assignGroupByPerson.setAssignAmount(amount);
                    assignGroupByPerson.setOffDaysNote(offDaysNote.substring(1));
                    resultList.add(assignGroupByPerson);
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
            assignGroupByPerson.setOffDaysNote(offDaysNote.substring(1));
            resultList.add(assignGroupByPerson);
        }
        return resultList;
    }

    public static void changeDataMode(RecordDetailsGroupByMonth record, DataMode dataMode) {
        record.setDataMode(dataMode);

        ContentValues contentValues = new ContentValues();
        contentValues.put("datamode", dataMode.ordinal());
        DbManager.update(RecordDetails.class,
                contentValues,
                "recordtime = ? and month = ?",
                String.valueOf(record.getRecordTime().toEpochSecond(ZoneOffset.ofHours(8))), String.valueOf(record.getMonth().toEpochDay()));
    }
}
