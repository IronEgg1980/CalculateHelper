package yzw.ahaqth.calculatehelper.moduls;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import yzw.ahaqth.calculatehelper.views.adapters.ItemViewTypeSupport;
import yzw.ahaqth.calculatehelper.views.adapters.MultiTypeModul;

public class Item extends BaseModul implements MultiTypeModul {
    private String name = "";

    public boolean isFocused = false;

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
        if(obj instanceof Item)
            return this.name.equals(((Item) obj).getName());
        return false;
    }

    @Override
    public int getItemViewType() {
        return ItemViewTypeSupport.TYPE_ITEM;
    }
}
