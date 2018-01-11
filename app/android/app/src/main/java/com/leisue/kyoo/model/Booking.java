package com.leisue.kyoo.model;

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
    private Long bookedDate;
    @SerializedName("bookingNo")
    @Expose
    private String bookingNo;


    public Booking(){
        this.id = "";
        this.name = "";
        this.contactNo = "";
        this.noOfCustomers = 0;
        this.bookedDate = 0l;
        this.bookingNo = "";
    }

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

    public Long getBookedDate() {
        return bookedDate;
    }

    public void setBookedDate(Long bookedDate) {
        this.bookedDate = bookedDate;
    }

    public String getBookingNo() {
        return bookingNo;
    }

    public void setBookingNo(String bookingNo) {
        this.bookingNo = bookingNo;
    }

}

