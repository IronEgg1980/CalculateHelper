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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import yzw.ahaqth.calculatehelper.views.adapters.BaseViewHolder;
import yzw.ahaqth.calculatehelper.views.adapters.ItemViewTypeSupport;
import yzw.ahaqth.calculatehelper.views.adapters.MultiTypeAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.MultiTypeModul;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;
import yzw.ahaqth.calculatehelper.views.dialogs.DropDownList;
import yzw.ahaqth.calculatehelper.views.dialogs.InputNumberDialog;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class InputActivity extends AppCompatActivity {
    protected class Adapter extends MultiTypeAdapter{

        Adapter() {
            super(list);
        }

        @Override
        public void bindData(final BaseViewHolder baseViewHolder, MultiTypeModul data) {
            if (data.getItemViewType() == ItemViewTypeSupport.TYPE_MONTH_ADDBUTTON) {
                baseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addMonth(baseViewHolder.getAdapterPosition());
                    }
                });
            } else if (data.getItemViewType() == ItemViewTypeSupport.TYPE_MONTH) {
                final AssignInMonthEntity entity = (AssignInMonthEntity) data;

                baseViewHolder.setText(R.id.monthTextView, entity.month.format(DateUtils.getYyyyM_Formatter()));
                baseViewHolder.setText(R.id.amountTextView, String.valueOf(entity.amount));

                final CheckBox checkBox = baseViewHolder.getView(R.id.checkbox);
                checkBox.setChecked(entity.isSelected);
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (manualFlag) {
                            if (entity.isSelected) {
                                entity.amount = 0;
                                entity.isSelected = false;
                                adpter.notifyItemChanged(baseViewHolder.getAdapterPosition());
                            } else {
                                manualAssign(baseViewHolder.getAdapterPosition());
                            }
                        } else {
                            entity.isSelected = checkBox.isChecked();
                            autoAssign();
                        }
                    }
                });

                ImageView imageView = baseViewHolder.getView(R.id.imageview);
                imageView.setVisibility(entity.isManualAssign ? View.VISIBLE : View.INVISIBLE);
                imageView.setEnabled(entity.isManualAssign);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        manualAssign(baseViewHolder.getAdapterPosition());
                    }
                });
            }
        }
    }

    private DropDownList dropDownList;
    private TextView itemNameTextView;
    private EditText itemAmountEditText;
    private RadioButton manualAssignRB;
    private MultiTypeAdapter adpter;
    private RecyclerView recyclerView;
    private List<MultiTypeModul> list;
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

    private void clearAll() {
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
        list.add(new MultiTypeModul() {
            @Override
            public int getItemViewType() {
                return ItemViewTypeSupport.TYPE_MONTH_ADDBUTTON;
            }
        });
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
        adpter = new Adapter();
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
                    for (MultiTypeModul modul : list) {
                        if (modul.getItemViewType() != ItemViewTypeSupport.TYPE_MONTH)
                            continue;
                        AssignInMonthEntity entity = (AssignInMonthEntity) modul;
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
        recyclerView = findViewById(R.id.month_list);
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

    private void addMonth(int position) {
        AssignInMonthEntity entity = (AssignInMonthEntity) list.get(position - 1);
        AssignInMonthEntity newEntity = new AssignInMonthEntity();
        newEntity.month = entity.month.minusMonths(1);
        list.add(position, newEntity);
        adpter.notifyItemRangeChanged(position, 2);
        recyclerView.smoothScrollToPosition(position + 1);
    }

    private void autoAssign() {
        totalAmount = 0;
        List<AssignInMonthEntity> selectedList = new ArrayList<>();
        if (!TextUtils.isEmpty(itemAmountEditText.getText())) {
            totalAmount = Double.parseDouble(itemAmountEditText.getText().toString().trim());
        }
        for (MultiTypeModul modul : list) {
            if (modul.getItemViewType() != ItemViewTypeSupport.TYPE_MONTH)
                continue;
            AssignInMonthEntity entity = (AssignInMonthEntity) modul;
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
        for (MultiTypeModul modul : list) {
            if (modul.getItemViewType() != ItemViewTypeSupport.TYPE_MONTH)
                continue;
            AssignInMonthEntity entity = (AssignInMonthEntity) modul;
            if (entity.isSelected) {
                value = BigDecimalHelper.minus(value, entity.amount);
            }
        }
        return value;
    }

    private void toggleAssignMode() {
        manualFlag = manualAssignRB.isChecked();
        for (MultiTypeModul modul : list) {
            if (modul.getItemViewType() != ItemViewTypeSupport.TYPE_MONTH)
                continue;
            AssignInMonthEntity entity = (AssignInMonthEntity) modul;
            entity.isSelected = false;
            entity.amount = 0;
            entity.isManualAssign = manualFlag;
        }
        adpter.notifyDataSetChanged();
    }

    private void manualAssign(int position) {
        AssignInMonthEntity entity = (AssignInMonthEntity) list.get(position);
        if (BigDecimalHelper.compare(getRemainValue(), 0) > 0 || entity.isSelected) {
            showInputDialog(position);
        } else {
            DialogFactory.getInfoDialog("所有金额已分配完毕").show(getSupportFragmentManager(), "info");
            entity.isSelected = false;
            adpter.notifyItemChanged(position);
        }
    }

    private void showInputDialog(final int position) {
        final AssignInMonthEntity entity = (AssignInMonthEntity) list.get(position);
        double maxValue = BigDecimalHelper.add(getRemainValue(), entity.amount);
        InputNumberDialog dialog = InputNumberDialog.getInstance(maxValue, entity.amount);
        dialog.setOnDismiss(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                if (confirmFlag) {
                    entity.amount = (double) values[0];
                    entity.isSelected = true;
                }
                adpter.notifyItemChanged(position);
            }
        });
        dialog.show(getSupportFragmentManager(), "inputnumber");
    }

    private void reset() {
        itemNameTextView.setText("");
        itemAmountEditText.setText("0.00");
        for (MultiTypeModul modul : list) {
            if (modul.getItemViewType() != ItemViewTypeSupport.TYPE_MONTH)
                continue;
            AssignInMonthEntity entity = (AssignInMonthEntity) modul;
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
        for (MultiTypeModul modul : list) {
            if (modul.getItemViewType() != ItemViewTypeSupport.TYPE_MONTH)
                continue;
            AssignInMonthEntity entity = (AssignInMonthEntity) modul;
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

                for (MultiTypeModul modul : list) {
                    if (modul.getItemViewType() != ItemViewTypeSupport.TYPE_MONTH)
                        continue;
                    AssignInMonthEntity entity = (AssignInMonthEntity) modul;
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
