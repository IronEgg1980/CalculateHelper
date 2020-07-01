package yzw.ahaqth.calculatehelper.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.R;
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
import yzw.ahaqth.calculatehelper.views.dialogs.SelectMonthDialog;
import yzw.ahaqth.calculatehelper.views.dialogs.ToastFactory;
import yzw.ahaqth.calculatehelper.views.interfaces.DataMode;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class InputDataFragment extends Fragment {
    private InputActivity activity;
    private MaterialEditText itemNameET, amountET;
    private RecyclerView  monthListRecyclerView;
    private List<RecordDetails> recordDetailsList;
    private MyAdapter<RecordDetails> monthListAdapter;
    private DropDownList<Item> dropDownList;
    private List<Item> items;
    private double totalAmount;
    private Handler mHander;
    private boolean isWaiting;
    private boolean isBreak;
    private String itemName;
    private double amount;
    private LoadingDialog loadingDialog;

    private void initial() {
        activity = (InputActivity) getActivity();
        recordDetailsList = new ArrayList<>();
        items = new ArrayList<>();
        monthListAdapter = new MyAdapter<RecordDetails>(recordDetailsList) {
            @Override
            public void bindData(final MyViewHolder myViewHolder, final RecordDetails data) {
                myViewHolder.setText(R.id.monthTextView, data.getMonth().format(DateUtils.getYyyyM_Formatter()));
                myViewHolder.setText(R.id.amountTextView, String.valueOf(data.getAmount()));
                myViewHolder.getView(R.id.imageview1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modifyRecordDetails(data);
                    }
                });
                myViewHolder.getView(R.id.imageview2).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleRecordDetails(myViewHolder.getAdapterPosition());
                    }
                });
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_input_recorddetails;
            }
        };

        this.mHander = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 0x11:
                        loadingDialog.show(activity.getSupportFragmentManager(), "loading");
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
                        confirmDialog.show(activity.getSupportFragmentManager(), "confirmDialog");
                        break;
                    case 0x02:
                        ToastFactory.showCenterToast(getContext(), "数据已保存");
                        reset();
                    case 0x10:
                        loadingDialog.dismiss();
                        break;
                }
                return true;
            }
        });
        this.loadingDialog = LoadingDialog.getInstance("正在保存数据...");
    }

    private void reset() {
        itemNameET.setText("");
        amountET.setText("");
        recordDetailsList.clear();
        monthListAdapter.notifyDataSetChanged();
    }

    private void generateRecordDetailsList(Object... localDates) {
        recordDetailsList.clear();
        if (localDates != null && localDates.length > 0) {
            for (Object o : localDates) {
                LocalDate localDate = (LocalDate) o;
                RecordDetails recordDetails = new RecordDetails();
                recordDetails.setRecordTime(activity.getRecordTime());
                recordDetails.setMonth(localDate);
                recordDetails.setAmount(0);
                recordDetailsList.add(recordDetails);
            }
        }
        autoAssign();
    }

    private void deleRecordDetails(int position){
        recordDetailsList.remove(position);
        autoAssign();
    }

    private void modifyRecordDetails(final RecordDetails recordDetails) {
        final double value = BigDecimalHelper.add(recordDetails.getAmount(),getRemainValue());
        InputNumberDialog dialog = InputNumberDialog.getInstance(totalAmount,value);
        dialog.setOnDismiss(new DialogCallback() {
            @Override
            public void onDismiss(boolean confirmFlag, Object... values) {
                if (confirmFlag) {
                    double value1 = (double) values[0];
                    recordDetails.setAmount(value1);
                    monthListAdapter.notifyDataSetChanged();
                }
            }
        });
        dialog.show(activity.getSupportFragmentManager(),"inpunumber");
    }

    private void showItemList() {
        items.addAll(DbManager.findAll(Item.class));
        if (items.isEmpty()) {
            DialogFactory dialogFactory = DialogFactory.getInfoDialog("项目列表为空，请先录入项目信息");
            dialogFactory.setDialogCallback(new DialogCallback() {
                @Override
                public void onDismiss(boolean confirmFlag, Object... values) {
                    startActivity(new Intent(getContext(), SetupActivity.class));
                }
            });
            dialogFactory.show(activity.getSupportFragmentManager(), "itemlistempty");
        } else {
            dropDownList.show();
        }
    }

    private boolean verifyInput() {
        if (TextUtils.isEmpty(itemNameET.getText())) {
            itemNameET.setError("请先选择项目");
            return false;
        }
        if (TextUtils.isEmpty(amountET.getText())) {
            amountET.setError("请输入金额");
            return false;
        }
        amount = 0;
        amount = Double.parseDouble(amountET.getText().toString());
        if (BigDecimalHelper.compare(amount, 0) <= 0) {
            amountET.setError("金额有误");
            return false;
        }
        if(recordDetailsList.isEmpty()){
            ToastFactory.showCenterToast(getContext(),"请选择月份进行分配");
            return false;
        }
        for (RecordDetails entity : recordDetailsList) {
            amount = BigDecimalHelper.minus(amount, entity.getAmount());
        }
        if (BigDecimalHelper.compare(amount, 0) != 0) {
            DialogFactory.getInfoDialog("余额为："+amount+"，请核对分配数据。").show(activity.getSupportFragmentManager(), "infomation");
            return false;
        }
        return true;
    }

    private void save() {
        itemName = itemNameET.getText().toString();
        amount = Double.parseDouble(amountET.getText().toString());
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message1 = mHander.obtainMessage();
                message1.what = 0x11;
                mHander.sendMessage(message1);
                for (RecordDetails entity : recordDetailsList) {
                    RecordDetails details = DbManager.findOne(RecordDetails.class, "itemname = ? and recordtime = ? and month = ?",
                            itemName, String.valueOf(activity.getRecordTime().toEpochSecond(ZoneOffset.ofHours(8))), String.valueOf(entity.getMonth().toEpochDay()));
                    if (details != null) {
                        isBreak = false;
                        isWaiting = true;

                        Message message = mHander.obtainMessage();
                        message.what = 0x01;
                        Bundle bundle = new Bundle();
                        bundle.putString("itemname", itemName);
                        bundle.putString("month", entity.getMonth().format(DateUtils.getYyyyM_Formatter()));
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
                            mHander.sendEmptyMessage(0x10);
                            return;
                        } else {
                            details.setAmount(BigDecimalHelper.add(entity.getAmount(), details.getAmount()));
                            details.setDataMode(DataMode.UNASSIGNED);
                            details.update();
                        }
                    } else {
                        entity.setItemName(itemName);
                        entity.setDataMode(DataMode.UNASSIGNED);
                    }
                }
                DbManager.saveInput(recordDetailsList);

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

    private double getRemainValue() {
        totalAmount = 0;
        if (!TextUtils.isEmpty(amountET.getText())) {
            totalAmount = Double.parseDouble(amountET.getText().toString());
        }
        double value = totalAmount;
        for (RecordDetails entity : recordDetailsList) {
            value = BigDecimalHelper.minus(value, entity.getAmount());
        }
        return value;
    }

    private void autoAssign() {
        totalAmount = 0;
        if(!TextUtils.isEmpty(amountET.getText())){
            totalAmount = Double.parseDouble(amountET.getText().toString());
        }
        double per = 0;
        int count = recordDetailsList.size();
        if (count > 0) {
            per = BigDecimalHelper.divide(totalAmount, count, 2);
        }

        for (int i = 0, listSize = recordDetailsList.size(); i < listSize; i++) {
            RecordDetails entity = recordDetailsList.get(i);
            if (i == listSize - 1) {
                entity.setAmount(0);
                entity.setAmount(getRemainValue());
            } else {
                entity.setAmount(per);
            }
        }
        monthListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initial();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input_data_layout, container, false);
        itemNameET = view.findViewById(R.id.itemname_textview);
        dropDownList = new DropDownList<>(itemNameET, items);
        itemNameET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showItemList();
            }
        });
        amountET = view.findViewById(R.id.amount_textview);
        amountET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                autoAssign();
            }
        });
        monthListRecyclerView = view.findViewById(R.id.month_list_recyclerview);
        monthListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        monthListRecyclerView.setAdapter(monthListAdapter);

        view.findViewById(R.id.select_month).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SelectMonthDialog()
                        .setCallback(new DialogCallback() {
                            @Override
                            public void onDismiss(boolean confirmFlag, Object... values) {
                                generateRecordDetailsList(values);
                            }
                        })
                        .show(activity.getSupportFragmentManager(), "select_month");
            }
        });
        view.findViewById(R.id.confirm_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });
        return view;
    }
}
