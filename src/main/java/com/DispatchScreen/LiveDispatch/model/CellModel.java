package com.DispatchScreen.LiveDispatch.model;


public class CellModel {

    private String cellname;
    private int target;
    private int actual;

    public String getCellname() { return cellname; }
    public void setCellname(String cellname) { this.cellname = cellname; }

    public int getTarget() { return target; }
    public void setTarget(int target) { this.target = target; }

    public int getActual() { return actual; }
    public void setActual(int actual) { this.actual = actual; }
}
