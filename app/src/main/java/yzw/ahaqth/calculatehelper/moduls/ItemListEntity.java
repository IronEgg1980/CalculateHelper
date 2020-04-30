package yzw.ahaqth.calculatehelper.moduls;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.views.adapters.ItemViewTypeLayoutConverter;

public class ItemListEntity implements ItemViewTypeLayoutConverter {
    public Item item;

    public ItemListEntity(Item item){
        this.item = item;
    }

    @Override
    public int getItemViewType() {
        return 1;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_item;
    }
}
