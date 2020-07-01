package yzw.ahaqth.calculatehelper.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.Remain;
import yzw.ahaqth.calculatehelper.moduls.RemainDetails;
import yzw.ahaqth.calculatehelper.tools.DateUtils;
import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.adapters.MyAdapter;

public class ShowRemain extends AppCompatActivity {

    private List<RemainDetails> remainList;
    private MyAdapter<RemainDetails> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remain);
        initial();
        TextView textView = findViewById(R.id.titleTextView);
        textView.setText("余额查询");
        findViewById(R.id.navagationIco).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        TextView textView1 = findViewById(R.id.remain);
        Remain remain = DbManager.findFirst(Remain.class);
        String remainString = remain == null ? "合计：" + 0 : "合计：" + remain.getAmount();
        textView1.setText(remainString);
    }

    private void initial() {
        remainList = DbManager.find(RemainDetails.class,
                false,
                null,
                null,
                null,
                null,
                "month");
        adapter = new MyAdapter<RemainDetails>(remainList) {
            @Override
            public void bindData(MyViewHolder myViewHolder, RemainDetails data) {
                myViewHolder.setText(R.id.monthTextView, data.getMonth().format(DateUtils.getYyyyM_Formatter()));
                double amount = data.getVariableAmount();
                TextView textView = myViewHolder.getView(R.id.amountTextView);
                String amountString = amount > 0 ? "+" + amount : "-" + amount;
                textView.setTextColor(amount > 0 ? Color.GREEN : Color.RED);
                textView.setText(amountString);
            }

            @Override
            public int getLayoutId(int position) {
                return R.layout.item_remaindetails;
            }
        };
    }
}
