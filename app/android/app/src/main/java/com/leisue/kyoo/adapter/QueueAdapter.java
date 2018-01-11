package com.leisue.kyoo.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.leisue.kyoo.R;
import com.leisue.kyoo.model.Booking;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Queue adapter
 */

public class QueueAdapter extends FirestoreAdapter<QueueAdapter.ViewHolder> {

    public interface OnBookingSelectedListener {

        void onBookingSelected(DocumentSnapshot restaurant);

    }

    private OnBookingSelectedListener mListener;

    public QueueAdapter(Query query, OnBookingSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_booking, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.booking_no)
        TextView bookingNoView;

        @BindView(R.id.booking_name)
        TextView bookingNameView;

        @BindView(R.id.booking_contact_no)
        TextView bookingContactNoView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot, final OnBookingSelectedListener listener) {

            Booking booking = snapshot.toObject(Booking.class);
            Resources resources = itemView.getResources();

            bookingNoView.setText(booking.getBookingNo());
            bookingNameView.setText(booking.getName());
            bookingContactNoView.setText(booking.getContactNo());

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onBookingSelected(snapshot);
                    }
                }
            });
        }

    }
}
