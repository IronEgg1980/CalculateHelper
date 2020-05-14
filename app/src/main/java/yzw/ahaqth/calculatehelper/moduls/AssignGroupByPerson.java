package yzw.ahaqth.calculatehelper.moduls;

import java.time.LocalDateTime;

public final class AssignGroupByPerson {
    private LocalDateTime recordTime;
    private String personName;
    private double assignAmount;
    private String monthList;
    private String offDaysNote;

    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
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

    public String getOffDaysNote() {
        return offDaysNote;
    }

    public void setOffDaysNote(String offDaysNote) {
        this.offDaysNote = offDaysNote;
    }

    public String getMonthList() {
        return monthList;
    }

    public void setMonthList(String monthList) {
        this.monthList = monthList;
    }
}
