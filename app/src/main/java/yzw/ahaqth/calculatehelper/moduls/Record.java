package yzw.ahaqth.calculatehelper.moduls;

import java.time.LocalDateTime;

import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;

public final class Record extends BaseModul {
    private LocalDateTime recordTime;
    private double totalAmount;
    private int personCount;

    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void putIntoTotalAmount(double amount){
        this.totalAmount = BigDecimalHelper.add(this.totalAmount,amount);
    }

    public int getPersonCount() {
        return personCount;
    }

    public void setPersonCount(int personCount){
        this.personCount = Math.max(this.personCount, personCount);
    }
}
