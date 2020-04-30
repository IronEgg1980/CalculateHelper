package yzw.ahaqth.calculatehelper.moduls;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TempRecordDetails extends BaseModul {
    public final static int MODE_ADD = 1;
    public final static int MODE_MODIFY = 3;

    private LocalDateTime recordTime;
    private LocalDate month;
    private String itemName;
    private double amount;
    private int dataMode;

    public int getDataMode() {
        return dataMode;
    }

    public void setDataMode(int dataMode) {
        this.dataMode = dataMode;
    }

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
}
