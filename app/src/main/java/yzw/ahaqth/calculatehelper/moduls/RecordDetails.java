package yzw.ahaqth.calculatehelper.moduls;

import java.time.LocalDate;
import java.time.LocalDateTime;

import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.views.BrokenLineGraph;
import yzw.ahaqth.calculatehelper.views.interfaces.DataMode;

public class RecordDetails extends BaseModul implements BrokenLineGraph.BrokenLineGraphEntity {
    private LocalDateTime recordTime;
    private LocalDate month;
    private String itemName;
    private double amount;
    private DataMode dataMode;


    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }

    public LocalDate getMonth() {
        return month;
    }

    public void setMonth(LocalDate month) {
        this.month = month;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public DataMode getDataMode() {
        return dataMode;
    }

    public void setDataMode(DataMode dataMode) {
        this.dataMode = dataMode;
    }

    @Override
    public String getLabel() {
        return month.format(DateUtils.getYyyyM_Formatter());
    }

    @Override
    public double getValue() {
        return amount;
    }
}
