package yzw.ahaqth.calculatehelper.moduls;

import java.time.LocalDate;

import yzw.ahaqth.calculatehelper.views.adapters.ItemViewTypeSupport;
import yzw.ahaqth.calculatehelper.views.adapters.MultiTypeModul;

public class AssignInMonthEntity implements MultiTypeModul {
    public LocalDate month;
    public double amount;
    public boolean isSelected = false;
    public boolean isManualAssign = false;

    @Override
    public int getItemViewType() {
        return ItemViewTypeSupport.TYPE_MONTH;
    }
}
