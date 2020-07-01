package yzw.ahaqth.calculatehelper.views.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.YearHeaderDecoration;
import yzw.ahaqth.calculatehelper.views.interfaces.DialogCallback;

public class SelectMonthDialog extends DialogFragment {
    private List<LocalDate> list;
    private SparseBooleanArray selectedIndex;
    private MyAdapter<LocalDate> adapter;
    private DialogCallback callback;

    public SelectMonthDialog setCallback(DialogCallback callback) {
        this.callback = callback;
        return this;
    }

    private void initialList() {
        int index = 0;
        selectedIndex = new SparseBooleanArray();
        list = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            int year = LocalDate.now().getYear() - i;
            for (int j = 0; j < 12; j++) {
                LocalDate localDate = LocalDate.of(year, 12 - j, 1);
                list.add(localDate);
                selectedIndex.put(index++, false);
            }
        }
        list.add(LocalDate.MIN);
    }

    private void confirm() {
        List<LocalDate> tmp = new ArrayList<>();
        for (int i = 0; i < selectedIndex.size(); i++) {
            if (selectedIndex.get(i)) {
                int index = selectedIndex.keyAt(i);
                tmp.add(list.get(index));
            }
        }

        if (callback != null)
            callback.onDismiss(true, tmp.toArray());
        dismiss();
    }

    private void addMonths() {
        int year = list.get(list.size() - 2).getYear() - 1;
        for (int i = 0; i < 12; i++) {
            int index = list.size() - 1;
            LocalDate localDate = LocalDate.of(year, 12 - i, 1);
            list.add(index, localDate);
            selectedIndex.put(index,false);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1600);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initialList();
        adapter = new MyAdapter<LocalDate>(list) {
            @Override
            public void bindData(MyViewHolder myViewHolder, LocalDate data) {
                final int index = myViewHolder.getAdapterPosition();
                final boolean selected = selectedIndex.get(index, false);
                if (data == LocalDate.MIN) {
                    myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            addMonths();
                        }
                    });
                } else {
                    myViewHolder.getView(R.id.selected_flag).setVisibility(selected ? View.VISIBLE : View.GONE);
//                    myViewHolder.setText(R.id.yearTextView, data.format(DateTimeFormatter.ofPattern("yyyy年", Locale.CHINA)));
                    myViewHolder.setText(R.id.monthTextView, data.format(DateTimeFormatter.ofPattern("M月", Locale.CHINA)));
                    myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectedIndex.put(index, !selected);
                            notifyItemChanged(index);
                        }
                    });
                }
            }

            @Override
            public int getLayoutId(int position) {
                if (list.get(position) == LocalDate.MIN) {
                    return R.layout.item_select_month_addbutton;
                }
                return R.layout.item_select_month;
            }
        };

        View view = inflater.inflate(R.layout.dialog_select_month, container, false);
        view.findViewById(R.id.close_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        view.findViewById(R.id.confirm_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == list.size() - 1)
                    return 4;
                return 1;
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new YearHeaderDecoration(list));
        return view;
    }
}
