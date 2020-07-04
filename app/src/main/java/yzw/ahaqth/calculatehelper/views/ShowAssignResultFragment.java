package yzw.ahaqth.calculatehelper.views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.AssignGroupByPerson;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;
import yzw.ahaqth.calculatehelper.views.adapters.MyDivideItemDecoration;
import yzw.ahaqth.calculatehelper.views.dialogs.ToastFactory;

public class ShowAssignResultFragment extends Fragment {

    private MyAdapter<AssignGroupByPerson> assignAdapter;
    private List<AssignGroupByPerson> list;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AssignActivity assignActivity = (AssignActivity) getActivity();
        list = DbManager.getAssignGroupByPersonList(assignActivity.getRecordTime());
        assignAdapter = new MyAdapter<AssignGroupByPerson>(list) {
            @Override
            public int getLayoutId(int position) {
                return R.layout.item_assign_record_details;
            }

            @Override
            public void bindData(MyViewHolder myViewHolder, AssignGroupByPerson data) {
                myViewHolder.setText(R.id.monthTextView, data.getPersonName());
                myViewHolder.setText(R.id.amountTextView, "总金额：" + data.getAssignAmount());
                myViewHolder.setText(R.id.noteTextView, data.getMonthList());
                myViewHolder.setText(R.id.datamodeTextView, data.getOffDaysNote());
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_assign_result_layout,container,false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(assignAdapter);
        recyclerView.addItemDecoration(new MyDivideItemDecoration());
        view.findViewById(R.id.copyResultView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard();
            }
        });
        return view;
    }

    private void copyToClipboard(){
        StringBuilder stringBuilder = new StringBuilder();
        for(AssignGroupByPerson a:list){
            stringBuilder.append(a.getPersonName())
                    .append("\t\t\t\t")
                    .append(a.getAssignAmount())
                    .append("\n");
        }

        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("label1",stringBuilder.toString());
        clipboardManager.setPrimaryClip(clipData);
        ToastFactory.showCenterToast(getContext(),"已复制");
    }
}
