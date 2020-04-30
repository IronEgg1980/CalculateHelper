package yzw.ahaqth.calculatehelper.manager;

import android.content.Context;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import yzw.ahaqth.calculatehelper.moduls.Record;

public class RecordDbManager extends DbManager<Record> {

    public RecordDbManager(Context context) {
        super(context, Record.class);
    }

    public Record findOne(LocalDateTime recordTime) {
        return findOne("recordtime = ?", String.valueOf(recordTime.toEpochSecond(ZoneOffset.ofHours(8))));
    }

    @Override
    public boolean isExist(Record record) {
        return findOne(record.getRecordTime()) != null;
    }
}
