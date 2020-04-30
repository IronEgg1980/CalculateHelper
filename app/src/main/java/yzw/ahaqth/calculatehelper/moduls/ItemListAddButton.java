package yzw.ahaqth.calculatehelper.moduls;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.views.adapters.ItemViewTypeLayoutConverter;

public class ItemListAddButton implements ItemViewTypeLayoutConverter {
    @Override
    public int getItemViewType() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_addbutton;
    }
}
