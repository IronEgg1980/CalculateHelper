package yzw.ahaqth.calculatehelper.manager;

import android.content.Context;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.moduls.AssignDetails;
import yzw.ahaqth.calculatehelper.moduls.AssignGroupByPerson;
import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;
import yzw.ahaqth.calculatehelper.tools.DateUtils;

public class AssignDetailsDbManager extends DbManager<AssignDetails> {
    public AssignDetailsDbManager(Context context) {
        super(context, AssignDetails.class);
    }

    public List<AssignDetails> find(LocalDateTime localDateTime, String personName) {
        return find("recordtime = ? and personname = ?", String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8))), personName);
    }

    public List<AssignDetails> find(LocalDateTime localDateTime) {
        return find("recordtime = ?", String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8))));
    }

    public String[] getPeopleList(LocalDateTime localDateTime) {
        List<AssignDetails> list = find(true, "recordtime = ?", new String[]{String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8)))},
                "personname", null, "personname");
        if (list.isEmpty())
            return null;
        String[] results = new String[list.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = list.get(i).getPersonName();
        }
        return results;
    }

    public List<AssignGroupByPerson> getAssignGroupByPersonList(LocalDateTime localDateTime) {
        String[] pepole = getPeopleList(localDateTime);
        List<AssignGroupByPerson> resultList = new ArrayList<>();
        if (pepole != null) {
            for (String person : pepole) {
                List<AssignDetails> findList = find(false, "recordtime = ? and personname = ?",
                        new String[]{String.valueOf(localDateTime.toEpochSecond(ZoneOffset.ofHours(8))),person},
                        null, null, "month");
                if (findList.isEmpty())
                    continue;
                AssignGroupByPerson assignGroupByPerson = new AssignGroupByPerson();
                assignGroupByPerson.setRecordTime(localDateTime);
                assignGroupByPerson.setPersonName(person);
                double assignAmount = 0;
                StringBuilder monthList = new StringBuilder();
                StringBuilder offDaysNote = new StringBuilder();
                for (AssignDetails assignDetails : findList) {
                    assignAmount = BigDecimalHelper.add(assignAmount, assignDetails.getAssignAmount());
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
                assignGroupByPerson.setAssignAmount(assignAmount);
                assignGroupByPerson.setMonthList(monthList.substring(1));
                assignGroupByPerson.setOffDaysNote(offDaysNote.length() > 0 ? offDaysNote.substring(1) : "");
                resultList.add(assignGroupByPerson);
            }
        }
        return resultList;
    }

    @Override
    public boolean isExist(AssignDetails assignDetails) {
        return isExist(assignDetails.getRecordTime(), assignDetails.getPersonName());
    }

    public boolean isExist(LocalDateTime recordTime, String personname) {
        return findOne("recordtime = ? and personname = ?",
                String.valueOf(recordTime.toEpochSecond(ZoneOffset.ofHours(8))), personname) != null;
    }
}
