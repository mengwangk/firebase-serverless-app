package com.leisue.kyoo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Archive summary.
 */

public class ArchiveSummary {

    public static final String FIELD_FROM_DATE = "fromDate";
    public static final String FIELD_TO_DATE = "toDate";


    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("fromDate")
    @Expose
    private Long fromDate;

    @SerializedName("toDate")
    @Expose
    private Long toDate;

    @SerializedName("totalBookings")
    @Expose
    private Long totalBookings;

    private boolean selected;

    private boolean isVisible;

    public ArchiveSummary() {
        this.id = "";
        this.fromDate = 0l;
        this.toDate = 0l;
        this.totalBookings = 0l;

        // Not a clean way, please adapt to your model
        this.selected = false;
        this.isVisible = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getFromDate() {
        return fromDate;
    }

    public void setFromDate(Long fromDate) {
        this.fromDate = fromDate;
    }

    public Long getToDate() {
        return toDate;
    }

    public void setToDate(Long toDate) {
        this.toDate = toDate;
    }

    public Long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(Long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }
}
