package yzw.ahaqth.calculatehelper;

import android.content.Context;
import android.util.Log;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.manager.AssignDetailsDbManager;
import yzw.ahaqth.calculatehelper.manager.RecordDetailsDbManager;
import yzw.ahaqth.calculatehelper.moduls.AssignDetails;
import yzw.ahaqth.calculatehelper.moduls.RecorDetailsGroupByMonth;
import yzw.ahaqth.calculatehelper.moduls.Record;
import yzw.ahaqth.calculatehelper.moduls.RecordDetails;
import yzw.ahaqth.calculatehelper.moduls.RecordDetailsGroupByItem;
import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.views.interfaces.DataMode;

public class Test {
    String TAG = "殷宗旺";
    List<RecordDetails> list;
    RecordDetailsDbManager dbManager;
    AssignDetailsDbManager assignDetailsDbManager;

    public Test(Context context){
       dbManager = new RecordDetailsDbManager(context);
       assignDetailsDbManager = new AssignDetailsDbManager(context);
    }

    public void test(){
//        deleData();
//        saveData();
        findAll();
//        findRecordList();
//        findGroupList();
    }

    private void saveData(){
        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.systemDefault());
        LocalDate localDate = localDateTime.toLocalDate();
        list = new ArrayList<>();
        List<AssignDetails> assignDetails = new ArrayList<>();
        for(int i = 0;i<5;i++){
            for(int j = 0;j<20;j++){
                AssignDetails assign = new AssignDetails();
                assign.setRecordTime(localDateTime);
                assign.setPersonName("person"+j);
                assign.setMonth(localDate.plusMonths(i));
                assign.setAssignAmount(BigDecimalHelper.multiply(77.7,(j + 1),2));
                assign.setOffDays(j);
                assignDetails.add(assign);
            }
        }

       assignDetailsDbManager.save(assignDetails);

        for(int i = 0;i<3;i++){
            for(int j = 0;j<10;j++) {
                RecordDetails recordDetails = new RecordDetails();
                recordDetails.setRecordTime(localDateTime);
                recordDetails.setMonth(localDate.plusMonths(i));
                recordDetails.setItemName("itemname" +j);
                recordDetails.setAmount(BigDecimalHelper.multiplyOnFloor(99.9,j));
                recordDetails.setDataMode(j % 2 == 0 ? DataMode.UNASSIGNED : DataMode.ASSIGNED);
                list.add(recordDetails);
            }
        }
        for(int i = 0;i<2;i++){
            for(int j = 0;j<12;j++) {
                RecordDetails recordDetails = new RecordDetails();
                recordDetails.setRecordTime(localDateTime.plusDays(1));
                recordDetails.setMonth(localDate.plusMonths(i));
                recordDetails.setItemName("itemname" +j);
                recordDetails.setAmount(BigDecimalHelper.multiplyOnFloor(88.8,j));
                recordDetails.setDataMode(j % 2 == 1 ? DataMode.UNASSIGNED : DataMode.ASSIGNED);
                list.add(recordDetails);
            }
        }
        dbManager.save(list);
    }

    private void deleData(){
        dbManager.deleAll();
        assignDetailsDbManager.deleAll();
    }

    private void findAll(){
        List<RecordDetails> list = dbManager.findAll();
        show(list);
    }

    private void findRecordList(){
        List<Record> list = dbManager.findSumData();
        for(Record record :list){
            Log.d(TAG, "RecordTime ("+record.getRecordTime().format(DateUtils.getYyyyMdHHmmss_Formatter())+
                    ") PersonCount ("+record.getPersonCount()+
                    ") TotalAmount ("+record.getTotalAmount()+
                    ")\n");
        }
    }

    private void findGroupList(){
        LocalDateTime[] recordTimes = dbManager.getRecordTimeList();
        List<RecorDetailsGroupByMonth> list1 = dbManager.getRecordGroupByMonthList(recordTimes[0],DataMode.ASSIGNED);
        List<RecordDetailsGroupByItem> list2 = dbManager.getRecordGroupByItemList(recordTimes[0]);
        Log.d(TAG, "----------------------RecorDetailsGroupByMonth------------------------");
        for(RecorDetailsGroupByMonth record :list1){
            Log.d(TAG, "RecordTime ("+record.getRecordTime().format(DateUtils.getYyyyMdHHmmss_Formatter())+
                    ") Month ("+record.getMonth().format(DateUtils.getYyyyM_Formatter()) +
                    ") Amount ("+record.getTotalAmount()+
                    ") Note ("+record.getItemNote()+
                    ")\n");
        }
        Log.d(TAG, "----------------------RecordDetailsGroupByItem------------------------");
        for(RecordDetailsGroupByItem record:list2){
            Log.d(TAG, "RecordTime ("+record.getRecordTime().format(DateUtils.getYyyyMdHHmmss_Formatter())+
                    ") Item ("+record.getItemName()+
                    ") Amount ("+record.getTotalAmount()+
                    ") MonthList ("+record.getMonthNote()+
                    ")\n");

        }
    }

    private void show(List<RecordDetails> list){
        if(list == null || list.isEmpty()) {
            Log.d(TAG, "find no data !");
            return;
        }
        for(RecordDetails recordDetails : list){
            Log.d(TAG, "ItemName ("+recordDetails.getItemName()+
                    ") | RecordTime ("+recordDetails.getRecordTime().format(DateUtils.getYyyyMdHHmmss_Formatter())+
                    ") | Month ("+recordDetails.getMonth().format(DateUtils.getYyyyM_Formatter())+
                    ") | Amount ("+recordDetails.getAmount()+
                    ") | DataMode ("+recordDetails.getDataMode().getDescribe()+
                    ")\n");
        }
    }
}
