package yzw.ahaqth.calculatehelper.moduls;

import java.time.LocalDateTime;
import java.util.List;

import yzw.ahaqth.calculatehelper.tools.DbManager;

public class BackupEntity {
    private LocalDateTime backupTime;
    private List<Item> items;
    private List<Person> people;
    private Remain remain;
    private List<RemainDetails> remainDetails;
    private List<RecordDetails> recordDetails;
    private List<AssignDetails> assignDetails;

    public BackupEntity(){
        backupTime = LocalDateTime.now();
        items = DbManager.findAll(Item.class);
        people = DbManager.findAll(Person.class);
        remain = DbManager.findFirst(Remain.class);
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

    public Remain getRemain() {
        return remain;
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
