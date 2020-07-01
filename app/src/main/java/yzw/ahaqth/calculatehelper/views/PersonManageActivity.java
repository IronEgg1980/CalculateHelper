package yzw.ahaqth.calculatehelper.views;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.Person;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;
import yzw.ahaqth.calculatehelper.views.dialogs.SingleEditTextDialog;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class PersonManageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<Person> personList;
    private MyAdapter<Person> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_recyclerview_layout);
        TextView titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText("人员管理");
        findViewById(R.id.navagationIco).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        generateData();

        adapter = new MyAdapter<Person>(personList) {
            @Override
            public int getLayoutId(int position) {
                if(position == personList.size() - 1)
                    return R.layout.item_addbutton;
                return R.layout.item_person_list;
            }

            @Override
            public void bindData(final MyViewHolder myViewHolder, Person data) {
                if (myViewHolder.getAdapterPosition() == personList.size() - 1) {
                    myViewHolder.getView(R.id.button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            addPerson(myViewHolder.getAdapterPosition());
                        }
                    });
                } else{
                    final SwipeMenuLayout menuLayout = myViewHolder.getView(R.id.swipemenulayout);
                    myViewHolder.setText(R.id.item_person_list_name,data.getName());
                    myViewHolder.getView(R.id.edit).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            menuLayout.smoothClose();
                            editPerson(myViewHolder.getAdapterPosition());
                        }
                    });
                    myViewHolder.getView(R.id.dele).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            menuLayout.smoothClose();
                            delePerson(myViewHolder.getAdapterPosition());
                        }
                    });
                }
            }
        };
        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(position == personList.size() - 1)
                    return 2;
                return 1;
            }
        });
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void generateData(){
        personList = new ArrayList<>();
        personList.addAll(DbManager.findAll(Person.class));
        personList.add(new Person());
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
                if (DbManager.isExist(Person.class,"name = ?",s)) {
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
                String oldName = person.getName();
                if (TextUtils.isEmpty(s)) {
                    dialog.showError("请输入姓名！");
                    return;
                }
                if (!oldName.equals(s)) {
                    if (DbManager.isExist(Person.class,"name = ?",s)) {
                        dialog.showError("已存在该人员，请改名！");
                        return;
                    }
                    person.setName(s);
                    person.update();
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
//        DbManager.save(Person.class,person);
        person.save();
        personList.add(position,DbManager.findLast(Person.class));
        adapter.notifyDataSetChanged();
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
//                    DbManager.dele(Person.class,"name = ?",person.getName());
                    person.dele();
                    personList.remove(position);
                    adapter.notifyItemRemoved(position);
                }
            }
        });
        dialogFactory.show(getSupportFragmentManager(),"dele");
    }
}
