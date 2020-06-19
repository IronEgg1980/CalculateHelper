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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.AssignInMonthEntity;
import yzw.ahaqth.calculatehelper.moduls.Item;
import yzw.ahaqth.calculatehelper.moduls.RecordDetails;
import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.dialogs.DialogFactory;
import yzw.ahaqth.calculatehelper.views.dialogs.DropDownList;
import yzw.ahaqth.calculatehelper.views.dialogs.InputNumberDialog;
import yzw.ahaqth.calculatehelper.views.dialogs.LoadingDialog;
import yzw.ahaqth.calculatehelper.views.dialogs.ToastFactory;
import yzw.ahaqth.calculatehelper.views.interfaces.DataMode;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class InputActivity extends AppCompatActivity {
    protected class Adapter extends MyAdapter<AssignInMonthEntity> {

        Adapter() {
            super(list);
        }

        @Override
        public int getLayoutId(int position) {
            if(list.get(position).amount == Integer.MIN_VALUE){
                return R.layout.select_month_item_addbutton;
            }
            return R.layout.select_month_item;
        }

        @Override
        public void bindData(final MyViewHolder myViewHolder, AssignInMonthEntity data) {
            if (data.amount == Integer.MIN_VALUE) {
                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addMonth(myViewHolder.getAdapterPosition());
                    }
                });
            } else {
                final AssignInMonthEntity entity = (AssignInMonthEntity) data;

                myViewHolder.setText(R.id.monthTextView, entity.month.format(DateUtils.getYyyyM_Formatter()));
                myViewHolder.setText(R.id.amountTextView, String.valueOf(entity.amount));

                final CheckBox checkBox = myViewHolder.getView(R.id.checkbox);
                checkBox.setChecked(entity.isSelected);
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (manualFlag) {
                            if (entity.isSelected) {
                                entity.amount = 0;
                                entity.isSelected = false;
                                notifyItemChanged(myViewHolder.getAdapterPosition());
                            } else {
                                manualAssign(myViewHolder.getAdapterPosition());
                            }
                        } else {
                            entity.isSelected = checkBox.isChecked();
                            autoAssign();
                        }
                    }
                });

                ImageView imageView = myViewHolder.getView(R.id.imageview);
                imageView.setVisibility(entity.isManualAssign ? View.VISIBLE : View.INVISIBLE);
                imageView.setEnabled(entity.isManualAssign);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        manualAssign(myViewHolder.getAdapterPosition());
                    }
                });
            }
        }
    }

    private AppCompatToggleButton manulAssignTB;
    private DropDownList dropDownList;
    private TextView itemNameTextView;
    private EditText itemAmountEditText;
    private Adapter adpter;
    private RecyclerView recyclerView;
    private List<AssignInMonthEntity> list;
    private boolean manualFlag = false;
    private double totalAmount;
    private LocalDateTime recordTime; // 传入参数
    private Handler mHander;
    private boolean isWaiting;
    private boolean isBreak;
    private String itemName;
    private double amount;
    private List<Item> items;
    private List<RecordDetails> inputResults;
    private LoadingDialog loadingDialog;

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
        AssignInMonthEntity assignInMonthEntity = new AssignInMonthEntity();
        assignInMonthEntity.amount = Integer.MIN_VALUE;
        list.add(assignInMonthEntity);
    }

    private void initial() {
        this.mHander = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 0x11:
                        loadingDialog.show(getSupportFragmentManager(), "loading");
                        break;
                    case 0x01:
                        Bundle bundle = msg.getData();
                        String s = "已存在 " + bundle.getString("itemname") + bundle.getString("month") +
                                " 的记录，\n点击【确定】合并金额，【取消】返回重新选择项目。";
                        DialogFactory confirmDialog = DialogFactory.getConfirmDialog(s);
                        confirmDialog.setDialogCallback(new DialogCallback() {
                            @Override
                            public void onDismiss(boolean confirmFlag, Object... values) {
                                isWaiting = false;
                                isBreak = !confirmFlag;
                            }
                        });
                        confirmDialog.show(getSupportFragmentManager(), "confirmDialog");
                        break;
                    case 0x02:
                        ToastFactory.showCenterToast(InputActivity.this, "数据已保存");
                        reset();
                    case 0x10:
                        loadingDialog.dismiss();
                        break;
                }
                return true;
            }
        });
        this.inputResults = new ArrayList<>();
        this.loadingDialog = LoadingDialog.getInstance("正在保存数据...");
    }

    private void initialView() {
        TextView titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText("工作区 - 输入数据");
        findViewById(R.id.navagationIco).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        adpter = new Adapter();
        itemNameTextView = findViewById(R.id.item_name_textview);
        itemNameTextView.setText("");
        itemAmountEditText = findViewById(R.id.item_amount_edittext);
        itemAmountEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(itemAmountEditText.getWindowToken(), 1000);
                }
            }
        });
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
                        if (entity.amount == Integer.MIN_VALUE)
                            continue;
                        entity.amount = 0;
                        entity.isSelected = false;
                    }
                    adpter.notifyDataSetChanged();
                } else {
                    autoAssign();
                }
            }
        });
        manulAssignTB = findViewById(R.id.toggleAutoAssign);
        manulAssignTB.setChecked(false);
        manulAssignTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == list.size() - 1)
                    return 2;
                return 1;
            }
        });
        recyclerView = findViewById(R.id.month_list);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adpter);
        findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });
    }

    private void showItemList() {
        items.addAll(DbManager.findAll(Item.class));
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
        AssignInMonthEntity entity =list.get(position - 1);
        AssignInMonthEntity newEntity = new AssignInMonthEntity();
        newEntity.month = entity.month.minusMonths(1);
        list.add(position, newEntity);
        adpter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(position + 1);
    }

    private void autoAssign() {
        totalAmount = 0;
        List<AssignInMonthEntity> selectedList = new ArrayList<>();
        if (!TextUtils.isEmpty(itemAmountEditText.getText())) {
            totalAmount = Double.parseDouble(itemAmountEditText.getText().toString().trim());
        }
        for (AssignInMonthEntity entity : list) {
            if (entity.amount == Integer.MIN_VALUE)
                continue;
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
            if (entity.amount == Integer.MIN_VALUE)
                continue;
            if (entity.isSelected) {
                value = BigDecimalHelper.minus(value, entity.amount);
            }
        }
        return value;
    }

    private void toggleAssignMode() {
        manualFlag = manulAssignTB.isChecked();
        for (AssignInMonthEntity entity : list) {
            if (entity.amount == Integer.MIN_VALUE)
                continue;
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
        for (AssignInMonthEntity entity : list) {
            if (entity.amount == Integer.MIN_VALUE)
                continue;
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
            if (entity.amount == Integer.MIN_VALUE)
                continue;
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
                Message message1 = mHander.obtainMessage();
                message1.what = 0x11;
                mHander.sendMessage(message1);
                if (inputResults == null)
                    inputResults = new ArrayList<>();
                inputResults.clear();
                for (AssignInMonthEntity entity : list) {
                    if (entity.amount == Integer.MIN_VALUE)
                        continue;
                    if (entity.isSelected) {
                        RecordDetails details = DbManager.findOne(RecordDetails.class, "itemname = ? and recordtime = ? and month = ?",
                                itemName, String.valueOf(recordTime.toEpochSecond(ZoneOffset.ofHours(8))), String.valueOf(entity.month.toEpochDay()));
                        if (details != null) {
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
                                Message message0 = mHander.obtainMessage();
                                message0.what = 0x10;
                                mHander.sendMessage(message0);
                                return;
                            } else {
                                details.setAmount(BigDecimalHelper.add(entity.amount, details.getAmount()));
                                details.setDataMode(DataMode.UNASSIGNED);
                                details.update();
                            }
                        } else {
                            details = new RecordDetails();
                            details.setRecordTime(recordTime);
                            details.setMonth(entity.month);
                            details.setItemName(itemName);
                            details.setAmount(entity.amount);
                            details.setDataMode(DataMode.UNASSIGNED);
                            inputResults.add(details);
                        }
                    }
                }
                DbManager.saveInput(inputResults);

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

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            recordTime = LocalDateTime.ofEpochSecond(bundle.getLong("recordtime"), 0, ZoneOffset.ofHours(8));
        } else {
            recordTime = LocalDateTime.now();
        }

        setContentView(R.layout.activity_input);
        createMonthList();
        initial();
        initialView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ToastFactory.showCenterToast(this, recordTime.format(DateUtils.getYyyyMdHHmmss_Formatter()));
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

}
