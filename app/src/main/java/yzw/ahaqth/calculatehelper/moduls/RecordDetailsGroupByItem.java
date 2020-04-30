package yzw.ahaqth.calculatehelper.moduls;

import java.time.LocalDateTime;

public final class RecordDetailsGroupByItem {
    private LocalDateTime recordTime;
    private String itemName;
    private double totalAmount;
    private String monthNote;

    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getMonthNote() {
        return monthNote;
    }

    public void setMonthNote(String monthNote) {
        this.monthNote = monthNote;
    }
}
