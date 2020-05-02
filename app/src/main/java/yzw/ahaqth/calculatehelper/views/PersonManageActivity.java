package yzw.ahaqth.calculatehelper.views;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.manager.PersonDbManager;
import yzw.ahaqth.calculatehelper.moduls.Person;
import yzw.ahaqth.calculatehelper.views.adapters.BaseViewHolder;
import yzw.ahaqth.calculatehelper.views.adapters.ItemViewTypeSupport;
import yzw.ahaqth.calculatehelper.views.adapters.MultiTypeAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.MultiTypeModul;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;
import yzw.ahaqth.calculatehelper.views.dialogs.SingleEditTextDialog;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class PersonManageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<MultiTypeModul> personList;
    private MultiTypeAdapter adapter;
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

        generateData();

        adapter = new MultiTypeAdapter(personList) {
            @Override
            public void bindData(final BaseViewHolder baseViewHolder, MultiTypeModul data) {
                Person person = (Person) data;
                if (data.getItemViewType() == ItemViewTypeSupport.TYPE_PERSON_BUTTON) {
                    baseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            addPerson(baseViewHolder.getAdapterPosition());
                        }
                    });
                } else if (data.getItemViewType() == ItemViewTypeSupport.TYPE_PERSON) {
                    baseViewHolder.setText(R.id.item_person_list_name,person.getName());
                    baseViewHolder.getView(R.id.edit).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            editPerson(baseViewHolder.getAdapterPosition());
                        }
                    });
                    baseViewHolder.getView(R.id.dele).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            delePerson(baseViewHolder.getAdapterPosition());
                        }
                    });
                }
            }
        };
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }

    private void generateData(){
        dbManager = new PersonDbManager(this);
        personList = new ArrayList<>();
        personList.addAll(dbManager.findAll());
        personList.add(new MultiTypeModul() {
            @Override
            public int getItemViewType() {
                return ItemViewTypeSupport.TYPE_PERSON_BUTTON;
            }
        });
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
        final Person person = (Person) personList.get(position);
        final SingleEditTextDialog dialog = new SingleEditTextDialog(this);
        dialog.setDialogCallback(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                String s = (String) values[0];
                String oldValue = person.getName();
                if (TextUtils.isEmpty(s)) {
                    dialog.showError("请输入姓名！");
                    return;
                }
                if (!oldValue.equals(s)) {
                    if (dbManager.isExist(s)) {
                        dialog.showError("已存在该人员，请改名！");
                        return;
                    }
                    person.setName(s);
                    dbManager.update(person,"name  = ?",person.getName());
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

    private void delePerson(final int position){
        final Person person = (Person) personList.get(position);
        DialogFactory dialogFactory = DialogFactory.getConfirmDialog("请确认",
                "是否删除 【"+person.getName()+"】 ？",
                R.drawable.warnning);
        dialogFactory.setDialogCallback(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                if(confirmFlag){
                    dbManager.dele(person);
                    personList.remove(position);
                    adapter.notifyItemRemoved(position);
                }
            }
        });
        dialogFactory.show(getSupportFragmentManager(),"dele");
    }

    private void itemClick(final int position, int id) {
        Person person = (Person) personList.get(position);
        if (id == R.id.edit) {
            editPerson(position);
        } else {
            DialogFactory dialogFactory = DialogFactory.getConfirmDialog("请确认",
                    "是否删除 【"+person.getName()+"】 ？",
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
