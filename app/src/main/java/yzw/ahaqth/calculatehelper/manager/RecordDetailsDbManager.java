package yzw.ahaqth.calculatehelper.manager;

import android.content.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.moduls.RecorDetailsGroupByMonth;
import yzw.ahaqth.calculatehelper.moduls.RecordDetails;
import yzw.ahaqth.calculatehelper.moduls.RecordDetailsGroupByItem;
import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;
import yzw.ahaqth.calculatehelper.tools.DateUtils;

public class RecordDetailsDbManager extends DbManager<RecordDetails> {
    public RecordDetailsDbManager(Context context) {
        super(context, RecordDetails.class);
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

    public Long[] getMonthList(LocalDateTime localDateTime) {
        List<RecordDetails> list = find(true, "recordtime = ?",
                new String[]{String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8)))
                }, "month", null, "month");
        if (list.isEmpty())
            return null;
        Long[] results = new Long[list.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = list.get(i).getMonth().toEpochDay();
        }
        return results;
    }

    public List<RecordDetails> find(LocalDateTime localDateTime, String itemName) {
        return find("recordtime = ? and itemname = ?", String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8))), itemName);
    }

    public List<RecordDetails> find(LocalDateTime localDateTime) {
        return find("recordtime = ?", String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8))));
    }

    public List<RecorDetailsGroupByMonth> getRecordGroupByMonthList(LocalDateTime localDateTime) {
        List<RecorDetailsGroupByMonth> list = new ArrayList<>();
        Long[] monthList = getMonthList(localDateTime);
        if (monthList != null) {
            for (Long month : monthList) {
                List<RecordDetails> findList = find("recordtime = ? and month = ?",
                        String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8))), String.valueOf(month));
                if (findList.isEmpty())
                    continue;
                RecorDetailsGroupByMonth groupByMonth = new RecorDetailsGroupByMonth();
                groupByMonth.setRecordTime(localDateTime);
                groupByMonth.setMonth(LocalDate.ofEpochDay(month));

                double totalAmount = 0;
                StringBuilder note = new StringBuilder();
                for (RecordDetails recordDetails : findList) {
                    totalAmount = BigDecimalHelper.add(totalAmount, recordDetails.getAmount());
                    note.append("、")
                            .append(recordDetails.getItemName())
                            .append(" (")
                            .append(recordDetails.getAmount())
                            .append(")");
                }
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
                    note.append("、")
                        .append(recordDetails.getMonth().format(DateUtils.getYyyyM_Formatter()))
                        .append(" (")
                        .append(recordDetails.getAmount())
                        .append(")");
                }
                groupByItem.setTotalAmount(totalAmount);
                groupByItem.setMonthNote(note.toString().substring(1));
                list.add(groupByItem);
            }
        }
        return list;
    }

    @Override
    public boolean isExist(RecordDetails recordDetails) {
        return isExist(recordDetails.getItemName(), recordDetails.getMonth());
    }

    public boolean isExist(String itemname, LocalDate month) {
        return !find("itemname = ? and month = ?", itemname, String.valueOf(month.toEpochDay())).isEmpty();
    }
}
