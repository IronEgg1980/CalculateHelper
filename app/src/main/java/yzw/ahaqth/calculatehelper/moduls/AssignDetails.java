package yzw.ahaqth.calculatehelper.moduls;

import java.time.LocalDate;
import java.time.LocalDateTime;

import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;

public class AssignDetails extends BaseModul {
    private LocalDateTime recordTime;
    private LocalDate month;
    private String personName;
    private double offDays;
    private double assignAmount;

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

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public double getAssignAmount() {
        return assignAmount;
    }

    public void setAssignAmount(double assignAmount) {
        this.assignAmount = assignAmount;
    }

    public double getOffDays() {
        return offDays;
    }

    public void setOffDays(double offDays) {
        this.offDays = offDays;
    }

    public double getAssignRatio(int maxDays){
        return BigDecimalHelper.divide(BigDecimalHelper.minus(maxDays,this.offDays),maxDays,2);
    }
}
