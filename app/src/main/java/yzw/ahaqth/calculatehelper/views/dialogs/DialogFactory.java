package yzw.ahaqth.calculatehelper.views.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class DialogFactory extends DialogFragment {
    private DialogCallback dialogCallback;
    private boolean confirmFlag = false;
    private String title = "", messge = "";
    private int mode = 1;
    private int icoId = R.drawable.info;
    private TextView messageTextView;

    public DialogFactory setDialogCallback(DialogCallback dialogCallback) {
        this.dialogCallback = dialogCallback;
        return this;
    }

    public static DialogFactory getConfirmDialog(String title, String message, int icoId) {
        DialogFactory dialogFactory = new DialogFactory();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("message", message);
        bundle.putInt("mode", 1);
        bundle.putInt("icoid", icoId);
        dialogFactory.setArguments(bundle);
        return dialogFactory;
    }

    public static DialogFactory getConfirmDialog(String message) {
        return getConfirmDialog("询问",message,-1);
    }

    public static DialogFactory getInfoDialog(String message) {
        DialogFactory dialogFactory = new DialogFactory();
        Bundle bundle = new Bundle();
        bundle.putString("title", "提示");
        bundle.putString("message", message);
        bundle.putInt("mode", 0);
        bundle.putInt("icoid", R.drawable.info);
        dialogFactory.setArguments(bundle);
        return dialogFactory;
    }

    public void changeMessage(String msg){
        if (messageTextView != null) {
            messageTextView.setText(msg);
        }
    }

    private DialogFactory() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            title = bundle.getString("title");
            messge = bundle.getString("message");
            mode = bundle.getInt("mode");
            icoId = bundle.getInt("icoid");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_common_layout, container, false);
        TextView titleTextView = view.findViewById(R.id.titleTextView);
        titleTextView.setText(title);
        if(icoId != -1) {
            Drawable drawable = getResources().getDrawable(icoId, null);
            drawable.setBounds(0, 0, 50, 50);
            titleTextView.setCompoundDrawables(drawable, null, null, null);
        }
        messageTextView = view.findViewById(R.id.messageTextView);
        messageTextView.setText(messge);
        TextView cancelButton = view.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmFlag = false;
                dismiss();
            }
        });
        cancelButton.setVisibility(mode == 1 ? View.VISIBLE : View.GONE);
        TextView confirmButton = view.findViewById(R.id.confirm);
        confirmButton.setText(mode == 1 ? "确定" : "关闭");
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmFlag = true;
                dismiss();
            }
        });
        View divider = view.findViewById(R.id.view2);
        divider.setVisibility(mode == 1 ? View.VISIBLE : View.GONE);
        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dialogCallback != null) {
            dialogCallback.onDismiss(confirmFlag);
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
            }
        }
    }
}
