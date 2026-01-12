package com.DispatchScreen.LiveDispatch.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "dispatch_screen_master")
public class DispatchScreenMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")   // Correct primary key field
    private int id;

    @Column(name = "screen_no")
    private int screenNo;

    @Column(name = "plnt_id")
    private long plntId;

    @Column(name = "cellname")
    private String cellname;

    @Column(name = "part_id")
    private long partId;

    @Column(name = "description")
    private String description;

    @Column(name = "daily_target")
    private int dailyTarget;

    @Column(name = "from_date")
    @Temporal(TemporalType.DATE)
    private Date fromDate;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;

    // ------------------ GETTERS ------------------ //
    public int getId() { return id; }
    public int getScreenNo() { return screenNo; }
    public long getPlntId() { return plntId; }
    public String getCellname() { return cellname; }
    public long getPartId() { return partId; }
    public String getDescription() { return description; }
    public int getDailyTarget() { return dailyTarget; }
    public Date getFromDate() { return fromDate; }
    public String getUpdatedBy() { return updatedBy; }
    public Date getUpdatedOn() { return updatedOn; }

    // ------------------ SETTERS ------------------ //
    public void setId(int id) { this.id = id; }
    public void setScreenNo(int screenNo) { this.screenNo = screenNo; }
    public void setPlntId(long plntId) { this.plntId = plntId; }
    public void setCellname(String cellname) { this.cellname = cellname; }
    public void setPartId(long partId) { this.partId = partId; }
    public void setDescription(String description) { this.description = description; }
    public void setDailyTarget(int dailyTarget) { this.dailyTarget = dailyTarget; }
    public void setFromDate(Date fromDate) { this.fromDate = fromDate; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public void setUpdatedOn(Date updatedOn) { this.updatedOn = updatedOn; }
}
