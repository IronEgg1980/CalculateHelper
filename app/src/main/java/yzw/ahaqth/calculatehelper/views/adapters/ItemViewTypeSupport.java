package yzw.ahaqth.calculatehelper.views.adapters;

import yzw.ahaqth.calculatehelper.R;

public class ItemViewTypeSupport {
    public static final int TYPE_ITEM_BUTTON = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_PERSON = 2;
    public static final int TYPE_PERSON_BUTTON = 3;
    public static final int TYPE_RECORD = 4;

    public static int getLayoutId(int viewType){
        int layoutId = -1;
        switch (viewType){
            case TYPE_ITEM_BUTTON:
                layoutId = R.layout.item_additem;
                break;
            case TYPE_ITEM:
                layoutId = R.layout.item_item;
                break;
            case TYPE_PERSON_BUTTON:
                layoutId = R.layout.item_addbutton;
                break;
            case TYPE_PERSON:
                layoutId = R.layout.item_person_list;
                break;
        }
        return layoutId;
    }
}
