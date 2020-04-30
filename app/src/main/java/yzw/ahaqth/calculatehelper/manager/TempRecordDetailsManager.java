package yzw.ahaqth.calculatehelper.manager;

import android.content.Context;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.moduls.RecorDetailsGroupByMonth;
import yzw.ahaqth.calculatehelper.moduls.TempRecordDetails;
import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;

public class TempRecordDetailsManager extends DbManager<TempRecordDetails> {
    public TempRecordDetailsManager(Context context) {
        super(context, TempRecordDetails.class);
    }

    public boolean isExist() {
        return (!findAll().isEmpty());
    }

    public Long[] getMonthList() {
        List<TempRecordDetails> list = find(true, null,null, "month", null, "month");
        if (list.isEmpty())
            return null;
        Long[] results = new Long[list.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = list.get(i).getMonth().toEpochDay();
        }
        return results;
    }

    public List<RecorDetailsGroupByMonth> getRecordGroupByMonthList() {
        List<RecorDetailsGroupByMonth> list = new ArrayList<>();
        Long[] monthList = getMonthList();
        if (monthList != null) {
            for (Long month : monthList) {
                List<TempRecordDetails> findList = find("month = ?",String.valueOf(month));
                if (findList.isEmpty())
                    continue;
                RecorDetailsGroupByMonth groupByMonth = new RecorDetailsGroupByMonth();
                groupByMonth.setMonth(LocalDate.ofEpochDay(month));

                double totalAmount = 0;
                StringBuilder note = new StringBuilder();
                for (TempRecordDetails recordDetails : findList) {
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
}
