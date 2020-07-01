package yzw.ahaqth.calculatehelper.views.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class SingleEditTextDialog extends PopupWindow {
    private DialogCallback dialogCallback;
    private EditText editText;
    private TextView titleTextView;

    public void setDialogCallback(DialogCallback dialogCallback) {
        this.dialogCallback = dialogCallback;
    }

    public SingleEditTextDialog(Context context) {
        generateView(context);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setTouchable(true);
        setFocusable(true);
        setOutsideTouchable(false);
        setInputMethodMode(INPUT_METHOD_NEEDED);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(1000, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void setTitle(String title) {
        if (titleTextView != null) {
            titleTextView.setText(title);
        }
    }

    public void setIco(Drawable drawable){
        if(drawable == null)
            return;
        drawable.setBounds(0,0,50,50);
        if (titleTextView != null) {
            titleTextView.setCompoundDrawables(drawable,null,null,null);
        }
    }

    public void setHint(String hint){
        if (editText != null) {
            editText.setHint(hint);
        }
    }

    public void setEditText(String text){
        if (editText != null) {
            editText.setText(text);
        }
    }

    public void showError(String errorMessage){
        if (editText != null) {
            editText.setError(errorMessage);
        }
    }

    private void generateView(Context context) {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_input_single_edittext, null);
        titleTextView = view.findViewById(R.id.titleTextView);
        editText = view.findViewById(R.id.edittext);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(editText.getText())){
                   editText.setError("请输入数据！");
                }else if (dialogCallback != null) {
                    dialogCallback.onDismiss(true, editText.getText().toString().trim());
                }
            }
        });

        editText.requestFocus();
        setContentView(view);
    }
}
