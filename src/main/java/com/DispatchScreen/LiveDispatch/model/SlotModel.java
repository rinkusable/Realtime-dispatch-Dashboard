package com.DispatchScreen.LiveDispatch.model;

import java.util.List;

public class SlotModel {
    private String slotTime;
    private List<CellModel> cellData;

    public String getSlotTime() { return slotTime; }
    public List<CellModel> getCellData() { return cellData; }
    

    public void setSlotTime(String slotTime) { this.slotTime = slotTime; }
    public void setCellData(List<CellModel> cellData) { this.cellData = cellData; }
   
}
