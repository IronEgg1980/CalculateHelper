package yzw.ahaqth.calculatehelper.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.moduls.RecorDetailsGroupByMonth;
import yzw.ahaqth.calculatehelper.moduls.Record;
import yzw.ahaqth.calculatehelper.moduls.RecordDetails;
import yzw.ahaqth.calculatehelper.moduls.RecordDetailsGroupByItem;
import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.views.interfaces.DataMode;

public class RecordDetailsDbManager extends DbManager<RecordDetails> {
    public RecordDetailsDbManager(Context context) {
        super(context, RecordDetails.class);
    }

    public LocalDateTime[] getRecordTimeList() {
        List<RecordDetails> list = find(true, null, null, "recordtime", null, "recordtime");
        if (list.isEmpty())
            return null;
        LocalDateTime[] results = new LocalDateTime[list.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = list.get(i).getRecordTime();
        }
        return results;
    }

    public String[] getItemList(LocalDateTime localDateTime) {
        List<RecordDetails> list = find(true, "recordtime = ?",
                new String[]{String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8)))
                }, "itemname", null, "itemname");
        if (list.isEmpty())
            return null;
        String[] results = new String[list.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = list.get(i).getItemName();
        }
        return results;
    }

    public String[] getItemList() {
        List<RecordDetails> list = find(true, "datamode = ?",
                new String[]{String.valueOf(DataMode.UNASSIGNED.ordinal())
                }, "itemname", null, "itemname");
        if (list.isEmpty())
            return null;
        String[] results = new String[list.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = list.get(i).getItemName();
        }
        return results;
    }

    public Long[] getMonthList(LocalDateTime localDateTime, DataMode dataMode) {
        String selection = "";
        String[] args;
        if (dataMode == null) {
            selection = "recordtime = ?";
            args = new String[]{String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8)))};
        } else {
            selection = "recordtime = ? and datamode = ?";
            args = new String[]{String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8))), String.valueOf(dataMode.ordinal())};
        }

        List<RecordDetails> list = find(true, selection, args, "month", null, "month");
        if (list.isEmpty())
            return null;
        Long[] results = new Long[list.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = list.get(i).getMonth().toEpochDay();
        }
        return results;
    }

    public List<Record> findSumData() {
        LocalDateTime[] recordTimes = getRecordTimeList();
        List<Record> list = new ArrayList<>();
        if (recordTimes != null) {
            for (LocalDateTime recordTime : recordTimes) {
                Record record = findSumData(recordTime);
                if (record != null)
                    list.add(record);
            }
        }
        return list;
    }

    private Record findSumData(LocalDateTime recordTime) {
        List<RecordDetails> list = find(recordTime);
        if (list.isEmpty()) {
            return null;
        }
        int count = DbHelper.count(mContext, "assigndetails", "personname", "recordtime = ?",
                String.valueOf(recordTime.toEpochSecond(ZoneOffset.ofHours(8))));
        Record record = new Record();
        record.setRecordTime(recordTime);
        record.setPersonCount(count);
        double amount = 0;
        for (RecordDetails recordDetails : list) {
            amount = BigDecimalHelper.add(amount, recordDetails.getAmount());
        }
        record.setTotalAmount(amount);
        return record;
    }

    public List<RecordDetails> find(LocalDateTime localDateTime, String itemName) {
        return find(false, "recordtime = ? and itemname = ?",
                new String[]{String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8))), itemName}, null, null, "month");
    }

    public List<RecordDetails> find(LocalDateTime localDateTime) {
        return find("recordtime = ?", String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8))));
    }

    public List<RecorDetailsGroupByMonth> getRecordGroupByMonthList(LocalDateTime localDateTime) {
        return getRecordGroupByMonthList(localDateTime, null);
    }

    public List<RecorDetailsGroupByMonth> getRecordGroupByMonthList(LocalDateTime localDateTime, DataMode dataMode) {
        List<RecorDetailsGroupByMonth> list = new ArrayList<>();
        Long[] monthList = getMonthList(localDateTime, dataMode);

        if (monthList != null) {
            for (Long month : monthList) {
                String selection = "";
                String[] args;
                if (dataMode == null) {
                    selection = "recordtime = ? and month = ?";
                    args = new String[]{String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8))),
                            String.valueOf(month)};
                } else {
                    selection = "recordtime = ? and month = ? and datamode = ?";
                    args = new String[]{String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8))),
                            String.valueOf(month),
                            String.valueOf(dataMode.ordinal())};
                }
                List<RecordDetails> findList = find(false,selection,args,null,null,"itemname");
                if (findList.isEmpty())
                    continue;
                RecorDetailsGroupByMonth groupByMonth = new RecorDetailsGroupByMonth();
                groupByMonth.setRecordTime(localDateTime);
                groupByMonth.setMonth(LocalDate.ofEpochDay(month));
                double totalAmount = 0;
                DataMode mode = DataMode.UNASSIGNED;
                StringBuilder note = new StringBuilder();
                for (RecordDetails recordDetails : findList) {
                    totalAmount = BigDecimalHelper.add(totalAmount, recordDetails.getAmount());
                    note.append("\n")
                            .append(recordDetails.getItemName())
                            .append(" (")
                            .append(recordDetails.getAmount())
                            .append(")");
                    mode = recordDetails.getDataMode();
                }
                groupByMonth.setDataMode(mode);
                groupByMonth.setTotalAmount(totalAmount);
                groupByMonth.setItemNote(note.toString().substring(1));
                list.add(groupByMonth);
            }
        }
        return list;
    }

    public List<RecordDetailsGroupByItem> getRecordGroupByItemList(LocalDateTime localDateTime) {
        List<RecordDetailsGroupByItem> list = new ArrayList<>();
        String[] itemList = getItemList(localDateTime);
        if (itemList != null) {
            for (String itemName : itemList) {
                List<RecordDetails> findList = find(localDateTime, itemName);
                if (findList.isEmpty())
                    continue;
                RecordDetailsGroupByItem groupByItem = new RecordDetailsGroupByItem();
                groupByItem.setRecordTime(localDateTime);
                groupByItem.setItemName(itemName);

                double totalAmount = 0;
                StringBuilder note = new StringBuilder("");
                for (RecordDetails recordDetails : findList) {
                    totalAmount = BigDecimalHelper.add(totalAmount, recordDetails.getAmount());
                    note.append("\n")
                            .append(recordDetails.getMonth().format(DateUtils.getYyyyM_Formatter()))
                            .append(" : ")
                            .append(recordDetails.getAmount())
                            .append(" (")
                            .append(recordDetails.getDataMode().getDescribe())
                            .append(")");
                }
                groupByItem.setTotalAmount(totalAmount);
                groupByItem.setMonthNote(note.toString().substring(1));
                list.add(groupByItem);
            }
        }
        return list;
    }

    public List<RecordDetailsGroupByItem> getUnassignedRecordGroupByItemList() {
        List<RecordDetailsGroupByItem> list = new ArrayList<>();
        String[] itemList = getItemList();
        if (itemList != null) {
            for (String itemName : itemList) {
                List<RecordDetails> findList = find(false, "itemname = ? and datamode = ?",
                        new String[]{itemName, String.valueOf(DataMode.UNASSIGNED.ordinal())}, null, null, "month");
                if (findList.isEmpty())
                    continue;
                RecordDetailsGroupByItem groupByItem = new RecordDetailsGroupByItem();
                groupByItem.setRecordTime(findList.get(0).getRecordTime());
                groupByItem.setItemName(itemName);

                double totalAmount = 0;
                StringBuilder note = new StringBuilder("");
                for (RecordDetails recordDetails : findList) {
                    totalAmount = BigDecimalHelper.add(totalAmount, recordDetails.getAmount());
                    note.append("\n")
                            .append(recordDetails.getMonth().format(DateUtils.getYyyyM_Formatter()))
                            .append(" : ")
                            .append(recordDetails.getAmount())
                            .append(" (")
                            .append(recordDetails.getDataMode().getDescribe())
                            .append(")");
                }
                groupByItem.setTotalAmount(totalAmount);
                groupByItem.setMonthNote(note.toString().substring(1));
                list.add(groupByItem);
            }
        }
        return list;
    }

    public void update(LocalDateTime recordTime, LocalDate month, DataMode dataMode) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("datamode", String.valueOf(dataMode.ordinal()));
        database.update(tableName, contentValues, "recordtime = ? and month = ?",
                new String[]{String.valueOf(recordTime.toEpochSecond(ZoneOffset.ofHours(8))), String.valueOf(month.toEpochDay())});
        database.close();
    }

    public void updateAssigned(LocalDateTime recordTime, LocalDate month) {
        update(recordTime, month, DataMode.ASSIGNED);
    }

    @Override
    public boolean isExist(RecordDetails recordDetails) {
        return isExist(recordDetails.getItemName(), recordDetails.getRecordTime());
    }

    public boolean isExist(String itemname, LocalDateTime recordTime) {
        return !find("itemname = ? and recordtime = ?", itemname, String.valueOf(recordTime.toEpochSecond(ZoneOffset.ofHours(8)))).isEmpty();
    }
}
