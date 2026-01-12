package com.DispatchScreen.LiveDispatch.model;

public class PartModel {
    private long partId;
    private String description;
    private int target;
    private int actual;

    public PartModel(long partId, String description, int target, int actual) {
        this.partId = partId;
        this.description = description;
        this.target = target;
        this.actual = actual;
    }

    public long getPartId() { return partId; }
    public String getDescription() { return description; }
    public int getTarget() { return target; }
    public int getActual() { return actual; }

    public void setPartId(long partId) { this.partId = partId; }
    public void setDescription(String description) { this.description = description; }
    public void setTarget(int target) { this.target = target; }
    public void setActual(int actual) { this.actual = actual; }
}
