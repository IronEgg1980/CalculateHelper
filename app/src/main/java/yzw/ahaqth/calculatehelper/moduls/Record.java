package yzw.ahaqth.calculatehelper.moduls;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;

public final class Record {
    private LocalDateTime recordTime;
    private double totalAmount;
    private int personCount;

    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void putIntoTotalAmount(double amount){
        this.totalAmount = BigDecimalHelper.add(this.totalAmount,amount);
    }

    public int getPersonCount() {
        return this.personCount;
    }

    public void setPersonCount(int personCount){
        this.personCount = Math.max(this.personCount, personCount);
    }
}
