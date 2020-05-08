package yzw.ahaqth.calculatehelper.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.manager.AssignDetailsDbManager;
import yzw.ahaqth.calculatehelper.manager.PersonDbManager;
import yzw.ahaqth.calculatehelper.manager.RecordDetailsDbManager;
import yzw.ahaqth.calculatehelper.manager.RemainDbManager;
import yzw.ahaqth.calculatehelper.moduls.AssignDetails;
import yzw.ahaqth.calculatehelper.moduls.Person;
import yzw.ahaqth.calculatehelper.moduls.RecorDetailsGroupByMonth;
import yzw.ahaqth.calculatehelper.moduls.Remain;
import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.views.adapters.BaseAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.BaseViewHolder;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;
import yzw.ahaqth.calculatehelper.views.dialogs.InputNumberDialog;
import yzw.ahaqth.calculatehelper.views.dialogs.ToastFactory;
import yzw.ahaqth.calculatehelper.views.interfaces.DataMode;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class AssignActivity extends AppCompatActivity {
    private LocalDateTime recordTime;
    private RecyclerView recyclerView1;
    private AppCompatToggleButton autoAssignTB;
    private RelativeLayout bottomGroup;
    private Button confirmButton, backStepButton;
    private List<RecorDetailsGroupByMonth> recordDetailsGroupByMonthList;
    private BaseAdapter<RecorDetailsGroupByMonth> recordDetailsAdapter;
    private List<Person> personList;
    private BaseAdapter<Person> personAdapter;
    private int currentIndex, maxDay = 30;
    private RecordDetailsDbManager recordDetailsDbManager;

    private void initial() {
        this.currentIndex = -1;
        this.personList = new PersonDbManager(this).findAll();
        this.recordDetailsDbManager = new RecordDetailsDbManager(this);
        this.recordDetailsGroupByMonthList = recordDetailsDbManager.getRecordGroupByMonthList(recordTime, DataMode.UNASSIGNED);
        this.recordDetailsAdapter = new BaseAdapter<RecorDetailsGroupByMonth>(R.layout.assign_recorddetailslist_item, recordDetailsGroupByMonthList) {
            @Override
            public void bindData(final BaseViewHolder baseViewHolder, final RecorDetailsGroupByMonth data) {
                baseViewHolder.setText(R.id.monthTextView, data.getMonth().format(DateUtils.getYyyyM_Formatter()));
                baseViewHolder.setText(R.id.amountTextView, String.valueOf(data.getTotalAmount()));
                baseViewHolder.setText(R.id.noteTextView, data.getItemNote());
                baseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentIndex = baseViewHolder.getAdapterPosition();
                        maxDay = recordDetailsGroupByMonthList.get(currentIndex).getMonth().lengthOfMonth();
                        changeAdapter(2);
                    }
                });
            }
        };
        this.personAdapter = new BaseAdapter<Person>(R.layout.assign_item, personList) {
            @Override
            public void bindData(final BaseViewHolder baseViewHolder, final Person data) {
                baseViewHolder.setText(R.id.nameTextView, data.getName());
                baseViewHolder.setText(R.id.assignAmountTextView, "当前分配：" + data.assignAmout);
                baseViewHolder.setText(R.id.totalAssignAmountTextView, "总分配：" + data.totalAmount);
                baseViewHolder.setText(R.id.offDays, "请假：" + data.offDays);
                baseViewHolder.setText(R.id.assignRatio, "分配系数：" + data.assignRatio);
                baseViewHolder.getView(R.id.offDays).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showNumberInputPop(baseViewHolder, data);
                    }
                });
                final CheckBox checkBox = baseViewHolder.getView(R.id.checkbox);
                checkBox.setChecked(data.isSelected);
                ImageView imageView = baseViewHolder.getView(R.id.imageview);
                imageView.setVisibility(autoAssignTB.isChecked() ? View.INVISIBLE : View.VISIBLE);
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!autoAssignTB.isChecked()) {
                            if (data.isSelected) {
                                data.totalAmount = BigDecimalHelper.minus(data.totalAmount, data.assignAmout);
                                data.assignAmout = 0;
                                data.isSelected = false;
                                personAdapter.notifyItemChanged(baseViewHolder.getAdapterPosition());
                            } else {
                                manualAssign(baseViewHolder.getAdapterPosition());
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
                        manualAssign(baseViewHolder.getAdapterPosition());
                    }
                });
            }
        };
    }

    private void showNumberInputPop(final BaseViewHolder baseViewHolder, final Person person) {
        NumberInputPopWindow numberInputPopWindow = new NumberInputPopWindow(this, maxDay).setOnDisMiss(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                if (confirmFlag) {
                    Double value = (Double) values[0];
                    person.offDays = value;
                    person.assignRatio = BigDecimalHelper.divide(BigDecimalHelper.minus(maxDay, value), maxDay, 2);
                    baseViewHolder.setText(R.id.offDays, "请假：" + value);
                    baseViewHolder.setText(R.id.assignRatio, "分配系数：" + person.assignRatio);
                    autoAssign();
                }
            }
        });
        numberInputPopWindow.show(baseViewHolder.getView(R.id.offDays));
    }

    private void changeAdapter(int step) {
        bottomGroup.setVisibility(step == 1 ? View.GONE : View.VISIBLE);
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
            p.totalAmount = BigDecimalHelper.minus(p.totalAmount, p.assignAmout);
            p.assignAmout = 0;
        }

        if (totalRatio != 0) {
            double per = BigDecimalHelper.divide(totalAmount, totalRatio);
            for (Person p : personList) {
                if (p.isSelected) {
                    double value = BigDecimalHelper.multiplyOnFloor(per, p.assignRatio);
                    p.assignAmout = value;
                    p.totalAmount = BigDecimalHelper.add(p.totalAmount, value);
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
                    person.totalAmount = BigDecimalHelper.add(person.totalAmount, person.assignAmout);
                    person.isSelected = true;
                }
                personAdapter.notifyItemChanged(position);
            }
        });
        dialog.show(getSupportFragmentManager(), "inputnumber");
    }

    private boolean save() {
        List<AssignDetails> list = new ArrayList<>();
        RecorDetailsGroupByMonth record = recordDetailsGroupByMonthList.get(currentIndex);
        double totalAssigned = 0;
        for (Person person : personList) {
            if (person.isSelected) {
                totalAssigned = BigDecimalHelper.add(totalAssigned, person.assignAmout);

                AssignDetails assignDetails = new AssignDetails();
                assignDetails.setRecordTime(record.getRecordTime());
                assignDetails.setMonth(record.getMonth());

                assignDetails.setPersonName(person.getName());
                assignDetails.setAssignAmount(person.assignAmout);
                assignDetails.setOffDays(person.offDays);

                list.add(assignDetails);
            }
        }
        double remainValue = BigDecimalHelper.minus(record.getTotalAmount(), totalAssigned);
        if(BigDecimalHelper.compare(remainValue,10)>0){
            DialogFactory.getInfoDialog("请将金额完全分配后再保存数据").show(getSupportFragmentManager(),"info");
            return false;
        }
        new AssignDetailsDbManager(this).save(list);
        new RemainDbManager(this).addRemainValue(remainValue);
        return true;
    }

    private void deleRecord() {
        RecorDetailsGroupByMonth recorDetailsGroupByMonth = recordDetailsGroupByMonthList.remove(currentIndex);
        currentIndex = -1;
        maxDay = 30;
        recordDetailsDbManager.updateAssigned(recordTime, recorDetailsGroupByMonth.getMonth());
    }

    private void initialView() {
        recyclerView1 = findViewById(R.id.recyclerview1);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        bottomGroup = findViewById(R.id.bottomGroup);
        autoAssignTB = findViewById(R.id.autoAssignToggleButton);
        confirmButton = findViewById(R.id.confirm);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(save()) {
                    deleRecord();
                    for (Person person : personList) {
                        person.assignAmout = 0;
                        person.isSelected = false;
                    }
                    changeAdapter(1);
                }
            }
        });
        backStepButton = findViewById(R.id.backStep);
        backStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Person person : personList) {
                    person.totalAmount = BigDecimalHelper.minus(person.totalAmount, person.assignAmout);
                    person.assignAmout = 0;
                    person.isSelected = false;
                }
                changeAdapter(1);
            }
        });
        autoAssignTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (Person person : personList) {
                    person.totalAmount = BigDecimalHelper.minus(person.totalAmount, person.assignAmout);
                    person.assignAmout = 0;
                    person.isSelected = false;
                }
                personAdapter.notifyDataSetChanged();
            }
        });

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

        setContentView(R.layout.activity_assign);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("按月份分配");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        initial();
        initialView();

    }

    @Override
    protected void onStart() {
        super.onStart();
        changeAdapter(1);
        ToastFactory.showCenterToast(this, recordTime.format(DateUtils.getYyyyMdHHmmss_Formatter()));
    }
}
