package yzw.ahaqth.calculatehelper.manager;

import android.content.Context;

import yzw.ahaqth.calculatehelper.moduls.Item;

public class ItemDbManager extends DbManager<Item> {
    public ItemDbManager(Context context) {
        super(context, Item.class);
    }

    @Override
    public void dele(Item item) {
        dele("name = ?",item.getName());
    }

    @Override
    public boolean isExist(Item item) {
        return isExist(item.getName());
    }

    public boolean isExist(String name){
        return findOne("name = ?", name) != null;
    }
}
