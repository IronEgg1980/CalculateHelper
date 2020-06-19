package yzw.ahaqth.calculatehelper.views.adapters;

import yzw.ahaqth.calculatehelper.R;

public enum ItemViewType {
    PERSON(R.layout.item_person_list),
    ITEM(R.layout.item_item),
    ITEM_BUTTON(R.layout.item_additem),
    PERSON_BUTTON(R.layout.item_addbutton),
    MONTH(R.layout.select_month_item),
    MONTH_ADDBUTTON(R.layout.select_month_item_addbutton)
    ;

    private int layoutId;
    ItemViewType(int id){
        this.layoutId = id;
    }

    public int getLayoutId() {
        return layoutId;
    }
}
