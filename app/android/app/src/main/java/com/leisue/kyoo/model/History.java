package com.leisue.kyoo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Historical booking model.
 */

public class History extends Booking {

    public enum ACTION {

        RETURN("return"),
        ARCHIVE("archive");

        private final String id;

        ACTION(final String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    public static final String FIELD_HISTORY_DATE = "historyDate";

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("queueId")
    @Expose
    private String queueId;

    @SerializedName("historyDate")
    @Expose
    private Long historyDate;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public Long getHistoryDate() {
        return historyDate;
    }

    public void setHistoryDate(Long historyDate) {
        this.historyDate = historyDate;
    }
}
