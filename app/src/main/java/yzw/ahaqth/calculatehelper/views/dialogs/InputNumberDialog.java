package yzw.ahaqth.calculatehelper.views.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class InputNumberDialog extends DialogFragment {
    private double maxValue;
    private double currentValue;
    private String title;
    private DialogCallback onDismiss;
    private boolean confirmFlag = false;
    private double resultValue = 0;
    private TextView titleTextView, maxAmountTextView;
    private EditText numberEdittext;
    private View cancel;
    private View confirm;


    public void setOnDismiss(DialogCallback onDismiss) {
        this.onDismiss = onDismiss;
    }

    public static InputNumberDialog getInstance(double maxValue, double value, String title) {
        InputNumberDialog dialog = new InputNumberDialog();
        Bundle bundle = new Bundle();
        bundle.putDouble("maxvalue", maxValue);
        bundle.putString("title", title);
        bundle.putDouble("value", value);
        dialog.setArguments(bundle);
        return dialog;
    }

    public static InputNumberDialog getInstance(double maxValue) {
        return getInstance(maxValue, 0, "输入金额");
    }

    public static InputNumberDialog getInstance(double maxValue, double value) {
        return getInstance(maxValue, value, "输入金额");
    }

    private InputNumberDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        title = "";
        if (bundle != null) {
            maxValue = bundle.getDouble("maxvalue");
            title = bundle.getString("title");
            currentValue = bundle.getDouble("value");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.input_number_dialog, container, false);
        titleTextView = view.findViewById(R.id.titleTextView);
        titleTextView.setText(title);
        maxAmountTextView = view.findViewById(R.id.maxAmountTextView);
        String s = "当前可分配最大金额：" + maxValue;
        maxAmountTextView.setText(s);
        view.findViewById(R.id.inputAllTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberEdittext.setText(String.valueOf(maxValue));
            }
        });
        numberEdittext = view.findViewById(R.id.number_edittext);
        numberEdittext.setText(String.valueOf(currentValue));
        cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmFlag = false;
                dismiss();
            }
        });
        confirm = view.findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });
        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismiss != null) {
            onDismiss.onDismiss(confirmFlag, resultValue);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            DisplayMetrics dm = new DisplayMetrics();
            Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = (int) (dm.widthPixels * 0.8);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(width, height);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
            numberEdittext.requestFocus();
            numberEdittext.selectAll();
            ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void confirm() {
        if (TextUtils.isEmpty(numberEdittext.getText())) {
            numberEdittext.setError("输入不能为空");
            return;
        }
        resultValue = Double.parseDouble(numberEdittext.getText().toString().trim());
        if (BigDecimalHelper.minus(maxValue, resultValue) < 0) {
            numberEdittext.setError("输入超出范围");
            return;
        }
        confirmFlag = resultValue > 0;
        dismiss();
    }
}
