package yzw.ahaqth.calculatehelper.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.manager.ItemDbManager;
import yzw.ahaqth.calculatehelper.manager.RecordDetailsDbManager;
import yzw.ahaqth.calculatehelper.manager.TempRecordDbManager;
import yzw.ahaqth.calculatehelper.manager.TempRecordDetailsManager;
import yzw.ahaqth.calculatehelper.moduls.AssignInMonthEntity;
import yzw.ahaqth.calculatehelper.moduls.Item;
import yzw.ahaqth.calculatehelper.moduls.TempRecord;
import yzw.ahaqth.calculatehelper.moduls.TempRecordDetails;
import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.views.adapters.MonthListAdpter;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;
import yzw.ahaqth.calculatehelper.views.dialogs.DropDownList;
import yzw.ahaqth.calculatehelper.views.dialogs.InputNumberDialog;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;
import yzw.ahaqth.calculatehelper.views.interfaces.ItemClickListener;

public class InputActivity extends AppCompatActivity {
    private DropDownList dropDownList;
    private TextView itemNameTextView;
    private EditText itemAmountEditText;
    private RadioButton manualAssignRB;
    private MonthListAdpter adpter;
    private List<AssignInMonthEntity> list;
    private boolean manualFlag = false;
    private double totalAmount;
    private LocalDateTime recordTime; // 传入参数
    private Handler mHander;
    private TempRecord tempRecord;
    private List<TempRecordDetails> tempRecordDetailsList;
    private RecordDetailsDbManager recordDetailsDbManager;
    private TempRecordDetailsManager tempRecordDetailsManager;
    private TempRecordDbManager tempRecordDbManager;
    private boolean isWaiting;
    private boolean isBreak;
    private String itemName;
    private double amount;
    private List<Item> items;

    private void clearAll(){
        tempRecordDetailsManager.deleAll();
        tempRecordDbManager.deleAll();
    }


    private void createMonthList() {
        LocalDate localDate = recordTime.toLocalDate();
        if (list == null) {
            list = new ArrayList<>();
        }
        list.clear();
        for (int i = 0; i < 6; i++) {
            AssignInMonthEntity entity = new AssignInMonthEntity();
            entity.amount = 0;
            entity.month = localDate.minusMonths(i);
            list.add(entity);
        }
    }

    private void initial() {
        this.mHander = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 0x01) {
                    Bundle bundle = msg.getData();
                    String s = "已存在 " + bundle.getString("month") +
                            bundle.getString("itemname") +
                            " 的记录，\n点击【确定】合并记录，【取消】返回重新选择月份。";
                    DialogFactory confirmDialog = DialogFactory.getConfirmDialog(s);
                    confirmDialog.setDialogCallback(new DialogCallback() {
                        @Override
                        public void onDismiss(boolean confirmFlag, Object... values) {
                            isWaiting = false;
                            isBreak = !confirmFlag;
                        }
                    });
                    confirmDialog.show(getSupportFragmentManager(), "confirmDialog");
                } else {
                    DialogFactory.getInfoDialog("已保存").show(getSupportFragmentManager(), "saved");
                    reset();
                }
                return true;
            }
        });
        this.recordDetailsDbManager = new RecordDetailsDbManager(this);
        this.tempRecordDetailsManager = new TempRecordDetailsManager(this);
        this.tempRecordDbManager = new TempRecordDbManager(this);
    }

    private void initialView() {
        adpter = new MonthListAdpter(list);
        adpter.setCheckBoxClicker(new ItemClickListener() {
            @Override
            public void onClick(int position, Object... values) {
                if (manualFlag) {
                    AssignInMonthEntity entity = list.get(position);
                    if (!entity.isSelected) {
                        entity.amount = 0;
                        adpter.notifyItemChanged(position);
                    }
                } else {
                    autoAssign();
                }
            }
        });
        adpter.setEditClicker(new ItemClickListener() {
            @Override
            public void onClick(int position, Object... values) {
                showInputDialog(position);
            }
        });
        itemNameTextView = findViewById(R.id.item_name_textview);
        itemNameTextView.setText("");
        itemAmountEditText = findViewById(R.id.item_amount_edittext);
        itemAmountEditText.setHint("0.00");
        itemAmountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (manualFlag) {
                    for (AssignInMonthEntity entity : list) {
                        entity.amount = 0;
                        entity.isSelected = false;
                    }
                    adpter.notifyDataSetChanged();
                } else {
                    autoAssign();
                }
            }
        });
        RadioButton autoAssignRB = findViewById(R.id.radio1);
        autoAssignRB.setChecked(true);
        manualAssignRB = findViewById(R.id.radio2);
        RadioGroup radioGroup = findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                toggleAssignMode();
            }
        });
        items = new ArrayList<>();
        dropDownList = new DropDownList<>(itemNameTextView, items);
        dropDownList.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                itemAmountEditText.selectAll();
                itemAmountEditText.requestFocus();
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        itemNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showItemList();
            }
        });
        RecyclerView recyclerView = findViewById(R.id.month_list);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adpter);
        findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });
    }

    private void showItemList() {
        items.addAll(new ItemDbManager(this).findAll());
        if (items.isEmpty()) {
            DialogFactory dialogFactory = DialogFactory.getInfoDialog("项目列表为空，请先录入项目信息");
            dialogFactory.setDialogCallback(new DialogCallback() {
                @Override
                public void onDismiss(boolean confirmFlag, Object... values) {
                    startActivity(new Intent(InputActivity.this, ItemManageActivity.class));
                }
            });
            dialogFactory.show(getSupportFragmentManager(), "itemlistempty");
        } else {
            dropDownList.show();
        }
    }

    private void autoAssign() {
        totalAmount = 0;
        List<AssignInMonthEntity> selectedList = new ArrayList<>();
        if (!TextUtils.isEmpty(itemAmountEditText.getText())) {
            totalAmount = Double.parseDouble(itemAmountEditText.getText().toString().trim());
        }
        for (AssignInMonthEntity entity : list) {
            entity.amount = 0;
            if (entity.isSelected) {
                selectedList.add(entity);
            }
        }

        double per = 0;
        int count = selectedList.size();
        if (count > 0) {
            per = BigDecimalHelper.divide(totalAmount, count, 2);
        }

        for (int i = 0, listSize = selectedList.size(); i < listSize; i++) {
            AssignInMonthEntity entity = selectedList.get(i);
            if (i == listSize - 1) {
                entity.amount = getRemainValue();
            } else {
                entity.amount = per;
            }
        }
        adpter.notifyDataSetChanged();
    }

    private double getRemainValue() {
        totalAmount = 0;
        if (!TextUtils.isEmpty(itemAmountEditText.getText())) {
            totalAmount = Double.parseDouble(itemAmountEditText.getText().toString().trim());
        }
        double value = totalAmount;
        for (AssignInMonthEntity entity : list) {
            if (entity.isSelected) {
                value = BigDecimalHelper.minus(value, entity.amount);
            }
        }
        return value;
    }

    private void toggleAssignMode() {
        manualFlag = manualAssignRB.isChecked();
        for (AssignInMonthEntity entity : list) {
            entity.isSelected = false;
            entity.amount = 0;
            entity.isManualAssign = manualFlag;
        }
        adpter.notifyDataSetChanged();
    }

    private void showInputDialog(final int position) {
        final AssignInMonthEntity entity = list.get(position);
        double maxValue = Math.max(getRemainValue(), entity.amount);
        if (BigDecimalHelper.compare(maxValue, 0) > 0 || entity.isSelected) {
            maxValue = BigDecimalHelper.add(maxValue, entity.amount);
            InputNumberDialog dialog = InputNumberDialog.getInstance(maxValue, entity.amount);
            dialog.setOnDismiss(new DialogCallback() {
                @Override
                public void onDismiss(boolean confirmFlag, Object... values) {
                    if (confirmFlag) {
                        entity.amount = (double) values[0];
                        entity.isSelected = true;
                        adpter.notifyItemChanged(position);
                    }
                }
            });
            dialog.show(getSupportFragmentManager(), "inputnumber");
        } else {
            DialogFactory.getInfoDialog("所有金额已分配完毕").show(getSupportFragmentManager(), "info");
        }
    }

    private void reset() {
        itemNameTextView.setText("");
        itemAmountEditText.setText("0.00");
        for (AssignInMonthEntity entity : list) {
            entity.isSelected = false;
            entity.amount = 0;
        }
        adpter.notifyDataSetChanged();
    }

    private boolean verifyInput() {
        if (TextUtils.isEmpty(itemNameTextView.getText())) {
            DialogFactory.getInfoDialog("请先选择项目").show(getSupportFragmentManager(), "unSelectInfo");
            return false;
        }
        if (TextUtils.isEmpty(itemAmountEditText.getText())) {
            itemAmountEditText.setError("请输入金额");
            return false;
        }
        amount = 0;
        amount = Double.parseDouble(itemAmountEditText.getText().toString());
        if (BigDecimalHelper.compare(amount, 0) <= 0) {
            itemAmountEditText.setError("金额有误");
            return false;
        }
        for (AssignInMonthEntity entity : list) {
            if (entity.isSelected) {
                amount = BigDecimalHelper.minus(amount, entity.amount);
            }
        }
        if (BigDecimalHelper.compare(amount, 0) > 0) {
            DialogFactory.getInfoDialog("还有金额未分配").show(getSupportFragmentManager(), "infomation");
            return false;
        }
        return true;
    }

    private void save() {
        itemName = itemNameTextView.getText().toString();
        amount = Double.parseDouble(itemAmountEditText.getText().toString());
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (tempRecordDetailsList == null)
                    tempRecordDetailsList = new ArrayList<>();

                for (AssignInMonthEntity entity : list) {
                    if (entity.isSelected) {
                        TempRecordDetails details = new TempRecordDetails();
                        details.setRecordTime(recordTime);
                        details.setMonth(entity.month);
                        details.setItemName(itemName);
                        details.setAmount(entity.amount);
                        if (recordDetailsDbManager.isExist(itemName, entity.month)) {
                            isBreak = false;
                            isWaiting = true;

                            Message message = mHander.obtainMessage();
                            message.what = 0x01;
                            Bundle bundle = new Bundle();
                            bundle.putString("itemname", itemName);
                            bundle.putString("month", entity.month.format(DateUtils.getYyyyM_Formatter()));
                            message.setData(bundle);
                            mHander.sendMessage(message);

                            while (isWaiting) {
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (isBreak) {
                                return;
                            } else {
                                details.setDataMode(TempRecordDetails.MODE_MODIFY);
                            }
                        } else {
                            details.setDataMode(TempRecordDetails.MODE_ADD);
                        }
                        tempRecordDetailsList.add(details);
                    }
                }

                tempRecord = tempRecordDbManager.findOne(null);
                if (tempRecord == null) {
                    tempRecord = new TempRecord();
                    tempRecord.setRecordTime(recordTime);
                }
                tempRecord.putIntoTotalAmount(amount);
                tempRecordDbManager.save(tempRecord);

                tempRecordDetailsManager.save(tempRecordDetailsList);

                tempRecordDetailsList = null;
                tempRecord = null;

                Message message = mHander.obtainMessage();
                message.what = 0x02;
                mHander.sendMessage(message);
            }
        }).start();
    }

    private void confirm() {
        if (verifyInput()) {
            save();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("数据输入");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        recordTime = LocalDateTime.now();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            recordTime = LocalDateTime.ofEpochSecond(bundle.getLong("recordtime"), 0, ZoneOffset.ofHours(8));
        }
        createMonthList();
        initial();
        initialView();

//        clearAll();// 记得要删除这句
    }
}
