package yzw.ahaqth.calculatehelper.moduls;

import java.time.LocalDate;
import java.time.LocalDateTime;

import yzw.ahaqth.calculatehelper.views.interfaces.DataMode;

public class RecordDetailsGroupByMonth {
    private LocalDateTime recordTime;
    private LocalDate month;
    private double totalAmount;
    private String itemNote;
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

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getItemNote() {
        return itemNote;
    }

    public void setItemNote(String itemNote) {
        this.itemNote = itemNote;
    }

    public DataMode getDataMode() {
        return dataMode;
    }

    public void setDataMode(DataMode dataMode) {
        this.dataMode = dataMode;
    }
}
