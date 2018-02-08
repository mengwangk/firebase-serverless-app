package com.leisue.kyoo.model;

/**
 * Archive Details
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ArchiveDetails implements Serializable {

    public static final String FIELD_QUEUE_NAME = "queueName";

    @SerializedName("bookedDate")
    @Expose
    private Long bookedDate;
    @SerializedName("queueName")
    @Expose
    private String queueName;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("queueId")
    @Expose
    private String queueId;
    @SerializedName("bookingNo")
    @Expose
    private String bookingNo;
    @SerializedName("contactNo")
    @Expose
    private String contactNo;
    @SerializedName("historyDate")
    @Expose
    private Long historyDate;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("noOfSeats")
    @Expose
    private Integer noOfSeats;
    @SerializedName("name")
    @Expose
    private String name;

    public Long getBookedDate() {
        return bookedDate;
    }

    public void setBookedDate(Long bookedDate) {
        this.bookedDate = bookedDate;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public String getBookingNo() {
        return bookingNo;
    }

    public void setBookingNo(String bookingNo) {
        this.bookingNo = bookingNo;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public Long getHistoryDate() {
        return historyDate;
    }

    public void setHistoryDate(Long historyDate) {
        this.historyDate = historyDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getNoOfSeats() {
        return noOfSeats;
    }

    public void setNoOfSeats(Integer noOfSeats) {
        this.noOfSeats = noOfSeats;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArchiveDetails that = (ArchiveDetails) o;

        if (bookedDate != null ? !bookedDate.equals(that.bookedDate) : that.bookedDate != null)
            return false;
        if (queueName != null ? !queueName.equals(that.queueName) : that.queueName != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (queueId != null ? !queueId.equals(that.queueId) : that.queueId != null) return false;
        if (bookingNo != null ? !bookingNo.equals(that.bookingNo) : that.bookingNo != null)
            return false;
        if (contactNo != null ? !contactNo.equals(that.contactNo) : that.contactNo != null)
            return false;
        if (historyDate != null ? !historyDate.equals(that.historyDate) : that.historyDate != null)
            return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (noOfSeats != null ? !noOfSeats.equals(that.noOfSeats) : that.noOfSeats != null)
            return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = bookedDate != null ? bookedDate.hashCode() : 0;
        result = 31 * result + (queueName != null ? queueName.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (queueId != null ? queueId.hashCode() : 0);
        result = 31 * result + (bookingNo != null ? bookingNo.hashCode() : 0);
        result = 31 * result + (contactNo != null ? contactNo.hashCode() : 0);
        result = 31 * result + (historyDate != null ? historyDate.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (noOfSeats != null ? noOfSeats.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ArchiveDetails{" +
            "bookedDate=" + bookedDate +
            ", queueName='" + queueName + '\'' +
            ", id='" + id + '\'' +
            ", queueId='" + queueId + '\'' +
            ", bookingNo='" + bookingNo + '\'' +
            ", contactNo='" + contactNo + '\'' +
            ", historyDate=" + historyDate +
            ", status='" + status + '\'' +
            ", noOfSeats=" + noOfSeats +
            ", name='" + name + '\'' +
            '}';
    }
}
