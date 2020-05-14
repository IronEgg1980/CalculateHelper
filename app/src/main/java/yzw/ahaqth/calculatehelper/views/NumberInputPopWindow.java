package yzw.ahaqth.calculatehelper.views;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rengwuxian.materialedittext.MaterialEditText;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class NumberInputPopWindow extends PopupWindow {
    private MaterialEditText tipsTextview;
    private Button num1;
    private Button num2;
    private Button num3;
    private Button num4;
    private Button num5;
    private Button num6;
    private Button num7;
    private Button num8;
    private Button num9;
    private Button num0;
    private Button dot;
    private Button backspace;
    private TextView cancel;
    private TextView confirm;
    private AppCompatActivity mActivity;
    private DialogCallback onDismiss;
    private double value = 0.0;
    private boolean confirmFlag = false;
    private int monthLength = 30;

    public NumberInputPopWindow(AppCompatActivity activity,int maxDay){
        mActivity = activity;
        monthLength = maxDay;
        createView();
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setTouchable(true);
        setOutsideTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if(onDismiss!=null)
                    onDismiss.onDismiss(confirmFlag,value);
            }
        });
    }

    public void createView(){
        View view = LayoutInflater.from(mActivity).inflate(R.layout.number_input_layout,new FrameLayout(mActivity),false);
        tipsTextview = view.findViewById(R.id.tips_textview);
        tipsTextview.setText("");
        num1 = view.findViewById(R.id.num1);
        num1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumber("1");
            }
        });
        num2 = view.findViewById(R.id.num2);
        num2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumber("2");
            }
        });
        num3 = view.findViewById(R.id.num3);
        num3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumber("3");
            }
        });
        num4 = view.findViewById(R.id.num4);
        num4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumber("4");
            }
        });
        num5 = view.findViewById(R.id.num5);
        num5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumber("5");
            }
        });
        num6 = view.findViewById(R.id.num6);
        num6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumber("6");
            }
        });
        num7 = view.findViewById(R.id.num7);
        num7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumber("7");
            }
        });
        num8 = view.findViewById(R.id.num8);
        num8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumber("8");
            }
        });
        num9 = view.findViewById(R.id.num9);
        num9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumber("9");
            }
        });
        num0 = view.findViewById(R.id.num0);
        num0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumber("0");
            }
        });
        dot = view.findViewById(R.id.dot);
        dot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumber(".");
            }
        });
        backspace = view.findViewById(R.id.backspace);
        backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backspace();
            }
        });
        backspace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                value = 0;
                tipsTextview.setText("");
                tipsTextview.setHelperText("");
                return true;
            }
        });
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
        setContentView(view);
    }



    public NumberInputPopWindow setOnDisMiss(DialogCallback onDismiss){
        this.onDismiss = onDismiss;
        return this;
    }

    private void showNumber(String input){
        String currentText = "";
        if(!TextUtils.isEmpty(tipsTextview.getText()))
            currentText = tipsTextview.getText().toString();
        else if(".".equals(input))
            return;
        if("0".equals(currentText) && "0".equals(input))
            return;
        currentText = currentText+input;
        try {
            value = Double.parseDouble(currentText);
            if(BigDecimalHelper.compare(value,monthLength) > 0){
                throw new IndexOutOfBoundsException("大于月份最大天数");
            }
            tipsTextview.setText(currentText);
            tipsTextview.setHelperText("");
        } catch (NumberFormatException e) {
            tipsTextview.setHelperText("输入错误");
            e.printStackTrace();
        } catch (IndexOutOfBoundsException ex){
            tipsTextview.setHelperText(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    private void backspace(){
        if(TextUtils.isEmpty(tipsTextview.getText()))
            return;
        String currentText = tipsTextview.getText().toString();
        currentText = currentText.substring(0,currentText.length() - 1);
        if(currentText.endsWith(".")){
            currentText = currentText.substring(0,currentText.length() - 1);
        }
        tipsTextview.setText(currentText);
        tipsTextview.setHelperText("");
        if(!currentText.isEmpty())
            value = Double.parseDouble(currentText);
    }

    private void confirm(){
        if(TextUtils.isEmpty(tipsTextview.getText())){
            confirmFlag = false;
            value = 0;
        }else {
            value = Double.parseDouble(tipsTextview.getText().toString());
            confirmFlag = true;
        }
        dismiss();
    }

    public void show(View view){
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        View contentView = getContentView();
        contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST),View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST));
        DisplayMetrics metric = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;     // 屏幕宽度（像素）
        int height = metric.heightPixels;   // 屏幕高度（像素）

        int x = location[0];
        if(x + contentView.getMeasuredWidth() > width){
            x = width - contentView.getMeasuredWidth();
        }
        int y = location[1] + view.getHeight();
        if(y + contentView.getMeasuredHeight() > height){
            y = height - contentView.getMeasuredHeight() + view.getHeight();
        }

        showAtLocation(view, Gravity.NO_GRAVITY,x,y);
    }
}
