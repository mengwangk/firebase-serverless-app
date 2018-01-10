package com.starsystems.kyoo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * POJO for booking.
 */
public class Booking {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("contactNo")
    @Expose
    private String contactNo;
    @SerializedName("noOfCustomers")
    @Expose
    private Integer noOfCustomers;
    @SerializedName("bookedDate")
    @Expose
    private Integer bookedDate;
    @SerializedName("bookingNo")
    @Expose
    private String bookingNo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public Integer getNoOfCustomers() {
        return noOfCustomers;
    }

    public void setNoOfCustomers(Integer noOfCustomers) {
        this.noOfCustomers = noOfCustomers;
    }

    public Integer getBookedDate() {
        return bookedDate;
    }

    public void setBookedDate(Integer bookedDate) {
        this.bookedDate = bookedDate;
    }

    public String getBookingNo() {
        return bookingNo;
    }

    public void setBookingNo(String bookingNo) {
        this.bookingNo = bookingNo;
    }

    @Override
    public String toString() {
        return "Booking{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", contactNo='" + contactNo + '\'' +
            ", noOfCustomers=" + noOfCustomers +
            ", bookedDate=" + bookedDate +
            ", bookingNo='" + bookingNo + '\'' +
            '}';
    }
}

