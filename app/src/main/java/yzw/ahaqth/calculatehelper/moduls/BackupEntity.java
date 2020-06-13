package yzw.ahaqth.calculatehelper.moduls;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import yzw.ahaqth.calculatehelper.tools.DbManager;
import yzw.ahaqth.calculatehelper.views.interfaces.DataMode;

public class BackupEntity implements Serializable {
    private LocalDateTime backupTime;
    private List<Item> items;
    private List<Person> people;
    private List<Remain> remains;
    private List<RemainDetails> remainDetails;
    private List<RecordDetails> recordDetails;
    private List<AssignDetails> assignDetails;

    public BackupEntity(){
        backupTime = LocalDateTime.now();
        items = DbManager.findAll(Item.class);
        people = DbManager.findAll(Person.class);
        remains = DbManager.findAll(Remain.class);
        remainDetails = DbManager.findAll(RemainDetails.class);
        recordDetails = DbManager.findAll(RecordDetails.class);
        assignDetails = DbManager.findAll(AssignDetails.class);
    }

    public LocalDateTime getBackupTime() {
        return backupTime;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<Person> getPeople() {
        return people;
    }

    public List<Remain> getRemains() {
        return remains;
    }

    public List<RemainDetails> getRemainDetails() {
        return remainDetails;
    }

    public List<RecordDetails> getRecordDetails() {
        return recordDetails;
    }

    public List<AssignDetails> getAssignDetails() {
        return assignDetails;
    }
}
