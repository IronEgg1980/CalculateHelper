package yzw.ahaqth.calculatehelper.moduls;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RemainDetails extends BaseModul {
    private LocalDateTime recordTime;
    private LocalDate month;
    private double variableAmount;
    private String variableNote;

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

    public double getVariableAmount() {
        return variableAmount;
    }

    public void setVariableAmount(double variableAmount) {
        this.variableAmount = variableAmount;
    }

    public String getVariableNote() {
        return variableNote;
    }

    public void setVariableNote(String variableNote) {
        this.variableNote = variableNote;
    }
}
