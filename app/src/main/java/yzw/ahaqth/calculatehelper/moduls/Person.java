package yzw.ahaqth.calculatehelper.moduls;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import yzw.ahaqth.calculatehelper.views.adapters.ItemViewTypeSupport;
import yzw.ahaqth.calculatehelper.views.adapters.MultiTypeModul;

public class Person extends BaseModul implements MultiTypeModul {
    private String name = "";

    public float assignRatio = 1.0f;
    public boolean isSelected = false;

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

    @Override
    public int getItemViewType() {
        return ItemViewTypeSupport.TYPE_PERSON;
    }
}
