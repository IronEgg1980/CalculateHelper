package yzw.ahaqth.calculatehelper.views.interfaces;

public enum DataMode {
    EMPTY("未定义状态"),
    UNASSIGNED("未分配"),
    ASSIGNED("已分配"),
    DELETED("已删除");

    public String getDescribe() {
        return describe;
    }

    private String describe;

    DataMode(String describe) {
        this.describe = describe;
    }
}
