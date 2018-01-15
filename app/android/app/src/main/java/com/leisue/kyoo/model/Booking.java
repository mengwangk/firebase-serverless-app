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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Booking booking = (Booking) o;

        if (id != null ? !id.equals(booking.id) : booking.id != null) return false;
        if (name != null ? !name.equals(booking.name) : booking.name != null) return false;
        if (contactNo != null ? !contactNo.equals(booking.contactNo) : booking.contactNo != null)
            return false;
        if (noOfCustomers != null ? !noOfCustomers.equals(booking.noOfCustomers) : booking.noOfCustomers != null)
            return false;
        if (bookedDate != null ? !bookedDate.equals(booking.bookedDate) : booking.bookedDate != null)
            return false;
        return bookingNo != null ? bookingNo.equals(booking.bookingNo) : booking.bookingNo == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (contactNo != null ? contactNo.hashCode() : 0);
        result = 31 * result + (noOfCustomers != null ? noOfCustomers.hashCode() : 0);
        result = 31 * result + (bookedDate != null ? bookedDate.hashCode() : 0);
        result = 31 * result + (bookingNo != null ? bookingNo.hashCode() : 0);
        return result;
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

