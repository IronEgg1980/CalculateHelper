package yzw.ahaqth.calculatehelper.moduls;

import java.time.LocalDate;
import java.time.LocalDateTime;

import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.views.BrokenLineGraph;

public class AssignDetails extends BaseModul implements BrokenLineGraph.BrokenLineGraphEntity {
    private LocalDateTime recordTime;
    private LocalDate month;
    private String personName;
    private double offDays;
    private double assignAmount;
    private String note;

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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getAssignRatio(int maxDays){
        return BigDecimalHelper.divide(BigDecimalHelper.minus(maxDays,this.offDays),maxDays,2);
    }

    @Override
    public String getLabel() {
        return month.format(DateUtils.getYyM_Formatter());
    }

    @Override
    public double getValue() {
        return assignAmount;
    }
}
