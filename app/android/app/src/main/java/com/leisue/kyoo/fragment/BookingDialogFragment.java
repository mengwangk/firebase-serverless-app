package com.leisue.kyoo.fragment;

import android.content.Context;
import android.os.Bundle;
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

    @OnClick(R.id.button_add)
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
        Booking booking = new Booking();

        if (rootView != null) {
            booking.setName(name.getText().toString());
            booking.setContactNo(contactNo.getText().toString());
            if (!TextUtils.isEmpty(noOfSeats.getText().toString()))
                booking.setNoOfCustomers(Integer.parseInt(noOfSeats.getText().toString()));
        }

        return booking;
    }

    void setBookingListener(BookingListener listener) {
        this.bookingListener = listener;
    }
}
