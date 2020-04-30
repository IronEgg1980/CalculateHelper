package yzw.ahaqth.calculatehelper.views.dialogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.manager.ItemDbManager;
import yzw.ahaqth.calculatehelper.moduls.Item;
import yzw.ahaqth.calculatehelper.views.MyDivideItemDecoration;

public class DropDownList<T> extends PopupWindow {
    private TextView anchorView;
    private List<T> mList;

    private class _Adapter extends RecyclerView.Adapter<_Adapter._VH>{

        @NonNull
        @Override
        public _VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new _VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.dropdown_list_item,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull _VH holder, int position) {
            final String name = String.valueOf(mList.get(position));
            holder.textView.setText(name);
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    anchorView.setText(name);
                    dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        private class _VH extends RecyclerView.ViewHolder{
            private TextView textView;
            public _VH(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.textview);
            }
        }
    }

    public DropDownList(@NonNull TextView anchorView,List<T> list){
        this.anchorView = anchorView;
        mList = list;
        initialView();
        setTouchable(true);
        setOutsideTouchable(true);
    }

    private void initialView(){
        View view = LayoutInflater.from(anchorView.getContext()).inflate(R.layout.dropdown_popwindow,null);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(anchorView.getContext()));
        recyclerView.addItemDecoration(new MyDivideItemDecoration());
        recyclerView.setAdapter(new _Adapter());
        setContentView(view);
    }
    public void show(){
        setWidth(anchorView.getWidth());
        setHeight(640);
        showAsDropDown(anchorView,-40,-40);
    }
}
