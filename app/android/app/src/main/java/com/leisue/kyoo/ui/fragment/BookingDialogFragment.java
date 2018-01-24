package com.leisue.kyoo.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.leisue.kyoo.R;
import com.leisue.kyoo.model.Booking;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Booking dialog.
 */

public class BookingDialogFragment extends DialogFragment {

    public static final String TAG = "BookingDialog";

    interface BookingListener {
        void onBooking(Booking booking);
    }

    private View rootView;

    @BindView(R.id.edit_name)
    EditText name;

    @BindView(R.id.edit_contact_no)
    EditText contactNo;

    @BindView(R.id.edit_no_of_seats)
    EditText noOfSeats;

    private BookingListener bookingListener;

    private Booking booking;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_booking, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (this.booking != null) {
            name.setText(booking.getName());
            contactNo.setText(booking.getContactNo());
            noOfSeats.setText(booking.getNoOfSeats().toString());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @OnClick(R.id.button_apply)
    public void onAddClicked() {
        if (bookingListener != null) {
            bookingListener.onBooking(getBooking());
        }

        dismiss();
    }

    @OnClick(R.id.button_cancel)
    public void onCancelClicked() {
        dismiss();
    }

    public Booking getBooking() {
        if (rootView != null) {
            if (this.booking == null) this.booking = new Booking();
            booking.setName(name.getText().toString());
            booking.setContactNo(contactNo.getText().toString());
            if (!TextUtils.isEmpty(noOfSeats.getText().toString()))
                booking.setNoOfSeats(Integer.parseInt(noOfSeats.getText().toString()));
        }

        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    void setBookingListener(BookingListener listener) {
        this.bookingListener = listener;
    }
}
