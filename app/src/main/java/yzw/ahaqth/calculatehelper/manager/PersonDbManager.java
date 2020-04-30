package yzw.ahaqth.calculatehelper.manager;

import android.content.Context;
import android.text.TextUtils;

import yzw.ahaqth.calculatehelper.moduls.Person;

public class PersonDbManager extends DbManager<Person> {
    public PersonDbManager(Context context){
        super(context,Person.class);
    }

    public Person findOne(String name){
        return findOne("name = ?",name);
    }

    @Override
    public void dele(Person person) {
        if(TextUtils.isEmpty(person.getName())){
            return;
        }
        dele("name = ?",person.getName());
    }

    @Override
    public boolean isExist(Person person) {
        return isExist(person.getName());
    }

    public boolean isExist(String name) {
        return findOne(name) != null;
    }
}
