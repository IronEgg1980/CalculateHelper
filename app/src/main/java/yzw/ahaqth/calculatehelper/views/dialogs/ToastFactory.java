package yzw.ahaqth.calculatehelper.views.dialogs;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import yzw.ahaqth.calculatehelper.R;

public class ToastFactory {
    private static Toast getToast(Context context,String message){
        View view = LayoutInflater.from(context).inflate(R.layout.toast_view,null);
        TextView textView = view.findViewById(R.id.textview);
        textView.setText(message);
        Toast toast = new Toast(context);
        toast.setView(view);
        return toast;
    }

    public static void showNormalToast(Context context,String message){
        Toast toast = getToast(context,message);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showCenterToast(Context context,String message){
       showToast(context,message,Toast.LENGTH_SHORT,Gravity.CENTER,0,0);
    }

    public static void showToast(Context context,String message,int duration,int gravity,int xOffset,int yOffset){
        Toast toast = getToast(context,message);
        toast.setGravity(gravity,xOffset,yOffset);
        toast.setDuration(duration);
        toast.show();
    }
}
