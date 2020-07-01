package yzw.ahaqth.calculatehelper.views;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.AssignDetails;
import yzw.ahaqth.calculatehelper.moduls.AssignGroupByPerson;
import yzw.ahaqth.calculatehelper.moduls.Person;
import yzw.ahaqth.calculatehelper.moduls.RecordDetailsGroupByMonth;
import yzw.ahaqth.calculatehelper.moduls.RemainDetails;
import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;
import yzw.ahaqth.calculatehelper.views.dialogs.InputNumberDialog;
import yzw.ahaqth.calculatehelper.views.dialogs.LoadingDialog;
import yzw.ahaqth.calculatehelper.views.dialogs.ToastFactory;
import yzw.ahaqth.calculatehelper.views.interfaces.DataMode;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class AssignActivity2 extends AppCompatActivity {
    private static final String TAG = "殷宗旺";
    private LocalDateTime recordTime;
    private RecyclerView recyclerView1;
    private AppCompatToggleButton autoAssignTB;
    private RelativeLayout bottomGroup;
    private Button confirmButton, backStepButton;
    private AppCompatCheckBox showAssignDetails;
    private List<AssignGroupByPerson> assignGroupByPersonList;
    private List<RecordDetailsGroupByMonth> recordDetailsGroupByMonthList;
    private MyAdapter<RecordDetailsGroupByMonth> recordDetailsAdapter;
    private MyAdapter<AssignGroupByPerson> assignAdapter;
    private Handler mHandler;
    private LoadingDialog loadingDialog;

    private List<Person> personList;
    private MyAdapter<Person> personAdapter;
    private int currentIndex, maxDay = 30;

    private void initial() {
        this.currentIndex = -1;
        this.personList = DbManager.findAll(Person.class);
        this.recordDetailsGroupByMonthList = DbManager.getRecordGroupByMonth(recordTime);
        this.assignGroupByPersonList = DbManager.getAssignGroupByPersonList(recordTime);

        this.recordDetailsAdapter = new MyAdapter<RecordDetailsGroupByMonth>(recordDetailsGroupByMonthList) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, final RecordDetailsGroupByMonth data) {
                myViewHolder.setText(R.id.monthTextView, data.getMonth().format(DateUtils.getYyyyM_Formatter()));
                myViewHolder.setText(R.id.amountTextView, String.valueOf(data.getTotalAmount()));
                myViewHolder.setText(R.id.noteTextView, data.getItemNote());
                myViewHolder.setText(R.id.datamodeTextView, data.getDataMode().getDescribe());
                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentIndex = myViewHolder.getAdapterPosition();
                        if (data.getDataMode() == DataMode.UNASSIGNED) {
                            changeAdapter(2);
                        } else {
                            showReAssignDialog();
                        }
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_assign_record_details;
            }
        };
        this.personAdapter = new MyAdapter<Person>(personList) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, final Person data) {
                myViewHolder.setText(R.id.nameTextView, data.getName());
                myViewHolder.setText(R.id.assignAmountTextView, "当前分配：" + data.assignAmout);
                myViewHolder.setText(R.id.offDays, "请假：" + data.offDays);
                myViewHolder.setText(R.id.assignRatio, "分配系数：" + data.assignRatio);
                myViewHolder.getView(R.id.offDays).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showNumberInputPop(myViewHolder, data);
                    }
                });
                final CheckBox checkBox = myViewHolder.getView(R.id.checkbox);
                checkBox.setChecked(data.isSelected);
                ImageView imageView = myViewHolder.getView(R.id.imageview);
                imageView.setVisibility(autoAssignTB.isChecked() ? View.INVISIBLE : View.VISIBLE);
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!autoAssignTB.isChecked()) {
                            if (data.isSelected) {
                                data.assignAmout = 0;
                                data.isSelected = false;
                                personAdapter.notifyItemChanged(myViewHolder.getAdapterPosition());
                            } else {
                                manualAssign(myViewHolder.getAdapterPosition());
                            }
                        } else {
                            data.isSelected = checkBox.isChecked();
                            autoAssign();
                        }
                    }
                });
                imageView.setEnabled(!autoAssignTB.isChecked());
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        manualAssign(myViewHolder.getAdapterPosition());
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_on_assign_work;
            }
        };
        this.assignAdapter = new MyAdapter<AssignGroupByPerson>( assignGroupByPersonList) {
            @Override
            public void bindData(MyViewHolder myViewHolder, AssignGroupByPerson data) {
                myViewHolder.setText(R.id.monthTextView, data.getPersonName());
                myViewHolder.setText(R.id.amountTextView, "总金额：" + data.getAssignAmount());
                myViewHolder.setText(R.id.noteTextView, "明细：" + data.getMonthList());
                myViewHolder.setText(R.id.datamodeTextView, data.getOffDaysNote());
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_assign_record_details;
            }
        };
        this.mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                loadingDialog.dismiss();
                if(msg.what == 0x01){
                    ToastFactory.showCenterToast(AssignActivity2.this,"数据已保存");
                    onSaved();
                }else if(msg.what == 0x02){
                    ToastFactory.showCenterToast(AssignActivity2.this,"保存失败");
                }
                return true;
            }
        });
    }

    private void showNumberInputPop(final MyAdapter.MyViewHolder myViewHolder, final Person person) {
        NumberInputPopWindow numberInputPopWindow = new NumberInputPopWindow(this, maxDay).setOnDisMiss(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                if (confirmFlag) {
                    Double value = (Double) values[0];
                    person.offDays = value;
                    person.assignRatio = BigDecimalHelper.divide(BigDecimalHelper.minus(maxDay, value), maxDay, 2);
                    myViewHolder.setText(R.id.offDays, "请假：" + value);
                    myViewHolder.setText(R.id.assignRatio, "分配系数：" + person.assignRatio);
                    autoAssign();
                }
            }
        });
        numberInputPopWindow.show(myViewHolder.getView(R.id.offDays));
    }

    private void changeAdapter(int step) {
        if(step == 2)
            maxDay = recordDetailsGroupByMonthList.get(currentIndex).getMonth().lengthOfMonth();
        bottomGroup.setVisibility(step == 1 ? View.INVISIBLE : View.VISIBLE);
        showAssignDetails.setVisibility(step == 1 ? View.VISIBLE : View.INVISIBLE);
        autoAssignTB.setChecked(true);
        recyclerView1.setAdapter(step == 1 ? recordDetailsAdapter : personAdapter);
    }

    private void autoAssign() {
        if (currentIndex < 0 || currentIndex >= recordDetailsGroupByMonthList.size() || personList.isEmpty())
            return;
        double totalAmount = recordDetailsGroupByMonthList.get(currentIndex).getTotalAmount();

        double totalRatio = 0;
        for (Person p : personList) {
            if (p.isSelected) {
                totalRatio = BigDecimalHelper.add(totalRatio, p.assignRatio);
            }
            p.assignAmout = 0;
        }

        if (totalRatio != 0) {
            double per = BigDecimalHelper.divide(totalAmount, totalRatio);
            for (Person p : personList) {
                if (p.isSelected) {
                    p.assignAmout = BigDecimalHelper.multiplyOnFloor(per, p.assignRatio);
                }
            }
        }
        personAdapter.notifyDataSetChanged();
    }

    private double getRemainValue() {
        double totalAmount = recordDetailsGroupByMonthList.get(currentIndex).getTotalAmount();
        for (Person p : personList) {
            if (p.isSelected) {
                totalAmount = BigDecimalHelper.minus(totalAmount, p.assignAmout);
            }
        }
        return totalAmount;
    }

    private void manualAssign(int position) {
        Person person = personList.get(position);
        if (BigDecimalHelper.compare(getRemainValue(), 0) > 0 || person.isSelected) {
            showInputDialog(position);
        } else {
            DialogFactory.getInfoDialog("所有金额已分配完毕").show(getSupportFragmentManager(), "info");
            person.isSelected = false;
            personAdapter.notifyItemChanged(position);
        }
    }

    private void showInputDialog(final int position) {
        final Person person = personList.get(position);
        double maxValue = BigDecimalHelper.add(getRemainValue(), person.assignAmout);
        InputNumberDialog dialog = InputNumberDialog.getInstance(maxValue, person.assignAmout);
        dialog.setOnDismiss(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                if (confirmFlag) {
                    person.assignAmout = (double) values[0];
                    person.isSelected = true;
                }
                personAdapter.notifyItemChanged(position);
            }
        });
        dialog.show(getSupportFragmentManager(), "inputnumber");
    }

    private void showReAssignDialog() {
        DialogFactory dialogFactory = DialogFactory.getConfirmDialog("该月份已分配，是否要重新分配？");
        dialogFactory.setDialogCallback(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                if (confirmFlag) {
                    reAssign();
                }
            }
        });
        dialogFactory.show(getSupportFragmentManager(), "reassign");
    }

    private void reAssign() {
        RecordDetailsGroupByMonth recordDetailsGroupByMonth = recordDetailsGroupByMonthList.get(currentIndex);
        if (DbManager.rollBackAssign(recordDetailsGroupByMonth.getRecordTime(), recordDetailsGroupByMonth.getMonth())) {
            recordDetailsGroupByMonth.setDataMode(DataMode.UNASSIGNED);
            recordDetailsAdapter.notifyItemChanged(currentIndex);
            changeAdapter(2);
        } else {
            ToastFactory.showCenterToast(this, "操作失败");
        }
    }

    private void checkData(){
        double remainValue = getRemainValue();
        if (BigDecimalHelper.compare(remainValue, 0) == 0) {
           save();
        }else{
            DialogFactory confirm = DialogFactory.getConfirmDialog("剩余金额：" + remainValue + "未分配，是否保存？\n（剩余金额将累计保存至余额）");
            confirm.setDialogCallback(new DialogCallback() {
                @Override
                public void onDismiss(boolean confirmFlag, Object... values) {
                    if (confirmFlag) {
                        save();
                    }
                }
            });
            confirm.show(getSupportFragmentManager(),"confirm");
        }
    }

    private void save() {
        loadingDialog.show(getSupportFragmentManager(),"saveLoading");
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<AssignDetails> list = new ArrayList<>();
                RecordDetailsGroupByMonth record = recordDetailsGroupByMonthList.get(currentIndex);
                for (Person person : personList) {
                    if (person.isSelected) {
                        AssignDetails assignDetails = new AssignDetails();
                        assignDetails.setRecordTime(record.getRecordTime());
                        assignDetails.setMonth(record.getMonth());
                        assignDetails.setPersonName(person.getName());
                        assignDetails.setAssignAmount(person.assignAmout);
                        assignDetails.setOffDays(person.offDays);
                        list.add(assignDetails);
                    }
                }

                RemainDetails remainDetails = new RemainDetails();
                remainDetails.setRecordTime(recordTime);
                remainDetails.setMonth(record.getMonth());
                remainDetails.setVariableAmount(getRemainValue());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(DbManager.saveAssignData(list, remainDetails)){
                    mHandler.sendEmptyMessage(0x01);
                }else{
                    mHandler.sendEmptyMessage(0x02);
                }
            }
        }).start();
    }

    private void showAssignDetails() {
        this.assignGroupByPersonList.clear();
        this.assignGroupByPersonList.addAll(DbManager.getAssignGroupByPersonList(recordTime));
        assignAdapter.notifyDataSetChanged();
        recyclerView1.setAdapter(assignAdapter);
    }

    private void onSaved() {
        recordDetailsGroupByMonthList.get(currentIndex).setDataMode(DataMode.ASSIGNED);
        currentIndex = -1;
        maxDay = 30;
        resetPersonList();
        changeAdapter(1);
    }

    private void initialView() {
        recyclerView1 = findViewById(R.id.recyclerview);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkData();
            }
        });
        backStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPersonList();
                changeAdapter(1);
            }
        });
        autoAssignTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                for (Person person : personList) {
//                    person.assignAmout = 0;
//                    person.isSelected = false;
//                }
                personAdapter.notifyDataSetChanged();
            }
        });
        showAssignDetails.setChecked(false);
        showAssignDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showAssignDetails.isChecked()) {
                    showAssignDetails.setText("返回");
                    showAssignDetails();
                } else {
                    showAssignDetails.setText("查看分配情况");
                    recyclerView1.setAdapter(recordDetailsAdapter);
                }
            }
        });
        loadingDialog = LoadingDialog.getInstance("正在保存数据...");
    }

    private void resetPersonList() {
        for (Person person : personList) {
            person.assignAmout = 0;
            person.offDays = 0;
            person.assignRatio = 1;
            person.isSelected = false;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong("recordtime", recordTime.toEpochSecond(ZoneOffset.ofHours(8)));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        recordTime = LocalDateTime.ofEpochSecond(savedInstanceState.getLong("recordtime"), 0, ZoneOffset.ofHours(8));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            recordTime = LocalDateTime.ofEpochSecond(bundle.getLong("recordtime"), 0, ZoneOffset.ofHours(8));
        } else {
            recordTime = LocalDateTime.now();
        }

        initial();
        initialView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        changeAdapter(1);
//        ToastFactory.showCenterToast(this, recordTime.format(DateUtils.getYyyyMdHHmmss_Formatter()));
    }
}
