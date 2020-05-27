package yzw.ahaqth.calculatehelper.views;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import yzw.ahaqth.calculatehelper.R;
import yzw.ahaqth.calculatehelper.moduls.AssignDetails;
import yzw.ahaqth.calculatehelper.moduls.RecordDetails;
import yzw.ahaqth.calculatehelper.tools.DbManager;

public class TestBrokenLineGraph extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_broken_line_graph);
        TextView textView = findViewById(R.id.titleView);
        Bundle bundle = getIntent().getExtras();
        BrokenLineGraph.BrokenLineGraphEntity[] datas = new BrokenLineGraph.BrokenLineGraphEntity[0];
        if(bundle != null){
            int mode = bundle.getInt("mode");
            if(mode == 1) {
               String personname = bundle.getString("personname");
               textView.setText(personname);
                List<AssignDetails> list =DbManager.find(AssignDetails.class,
                        false,
                        "personname = ?",
                        new String[]{personname},
                        null,
                        null,
                        "month");
                datas = list.toArray(new AssignDetails[0]);
            }else{
                String itemname = bundle.getString("itemname");
                textView.setText(itemname);
                List<RecordDetails> list = DbManager.find(RecordDetails.class,
                        false,
                        "itemname = ?",
                        new String[]{itemname},
                        null,
                        null,
                        "month");
                datas = list.toArray(new RecordDetails[0]);
            }
        }
        BrokenLineGraph brokenLineGraph = findViewById(R.id.brokenLineGraph);
        brokenLineGraph.setData(datas);
    }
}
