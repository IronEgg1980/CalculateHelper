package yzw.ahaqth.calculatehelper.moduls;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Person extends BaseModul{
    private String name = "";

    public double assignRatio = 1.0f;
    public boolean isSelected = false;
    public double assignAmout = 0;
    public double offDays = 0;
    public double extraAmount = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null)
            return false;
        if(obj instanceof Person){
            return this.name.equals(((Person) obj).getName());
        }
        return false;
    }
}
