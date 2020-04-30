package yzw.ahaqth.calculatehelper.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.manager.PersonDbManager;
import yzw.ahaqth.calculatehelper.moduls.Person;
import yzw.ahaqth.calculatehelper.views.adapters.PersonListAdapter;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;
import yzw.ahaqth.calculatehelper.views.dialogs.SingleEditTextDialog;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;
import yzw.ahaqth.calculatehelper.views.interfaces.ItemClickListener;

public class PersonManageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<Person> personList;
    private PersonListAdapter adapter;
    private PersonDbManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbar_recyclerview_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("成员管理");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        personList = new PersonDbManager(this).findAll();
        adapter = new PersonListAdapter(personList);
        adapter.setAddButtonClicker(new ItemClickListener() {
            @Override
            public void onClick(int position, Object... values) {
                addPerson(position);
            }
        });
        adapter.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(int position, Object... values) {
                itemClick(position, (Integer) values[0]);
            }
        });
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
        dbManager = new PersonDbManager(this);
    }

    private void addPerson(final int position) {
        final SingleEditTextDialog dialog = new SingleEditTextDialog(this);
        dialog.setDialogCallback(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                String s = (String) values[0];
                if (TextUtils.isEmpty(s)) {
                    dialog.showError("请输入姓名！");
                    return;
                }
                if (dbManager.isExist(s)) {
                    dialog.showError("已存在该人员，请改名！");
                    return;
                }
                addPerson(position, s);
                dialog.dismiss();
            }
        });
        dialog.showAtLocation(recyclerView, Gravity.BOTTOM, 0, 0);
        dialog.setTitle("新增人员");
        dialog.setHint("请输入姓名");
        dialog.setIco(getResources().getDrawable(R.drawable.person_add,null));
    }

    private void editPerson(final int position) {
        final Person person = personList.get(position);
        final SingleEditTextDialog dialog = new SingleEditTextDialog(this);
        dialog.setDialogCallback(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                String s = (String) values[0];
                if (TextUtils.isEmpty(s)) {
                    dialog.showError("请输入姓名！");
                    return;
                }
                if (!person.getName().equals(s)) {
                    if (dbManager.isExist(s)) {
                        dialog.showError("已存在该人员，请改名！");
                        return;
                    }
                    person.setName(s);
                    dbManager.update(person);
                    adapter.notifyItemChanged(position);
                }
                dialog.dismiss();
            }
        });
        dialog.showAtLocation(recyclerView, Gravity.BOTTOM, 0, 0);
        dialog.setTitle("修改信息");
        dialog.setIco(getResources().getDrawable(R.drawable.edit,null));
        dialog.setEditText(person.getName());
    }

    private void addPerson(int position, String name) {
        Person person = new Person();
        person.setName(name);
        dbManager.save(person);
        personList.add(person);
        adapter.notifyItemRangeChanged(position, 2);
        recyclerView.smoothScrollToPosition(position + 1);

    }

    private void delePerson(int position){
        Person person = personList.get(position);
        dbManager.dele(person);
        personList.remove(position);
        adapter.notifyItemRemoved(position);
    }

    private void itemClick(final int position, int id) {
        if (id == R.id.edit) {
            editPerson(position);
        } else {
            DialogFactory dialogFactory = DialogFactory.getConfirmDialog("请确认",
                    "是否删除 【"+personList.get(position).getName()+"】 ？",
                    R.drawable.warnning);
            dialogFactory.setDialogCallback(new DialogCallback() {
                @Override
                public void onDismiss(boolean confirmFlag, Object... values) {
                    if(confirmFlag){
                        delePerson(position);
                    }
                }
            });
            dialogFactory.show(getSupportFragmentManager(),"dele");
        }
    }
}
