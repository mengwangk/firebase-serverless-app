package com.leisue.kyoo.adapter;

import android.content.res.Resources;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.R;
import com.leisue.kyoo.model.Booking;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.Queue;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Queue adapter
 */

public class QueueAdapter extends FirestoreAdapter<QueueAdapter.ViewHolder> {

    public interface OnBookingSelectedListener {
        void onBookingSelected(Booking booking);
    }

    private OnBookingSelectedListener mListener;
    private Queue mQueue;

    public QueueAdapter(Queue queue, Query query, OnBookingSelectedListener listener) {
        super(query);
        mQueue = queue;
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        Booking booking;

        @BindView(R.id.booking_no)
        TextView bookingNoView;

        @BindView(R.id.booking_name)
        TextView bookingNameView;

        @BindView(R.id.booking_contact_no)
        TextView bookingContactNoView;

        @BindView(R.id.button_delete)
        ImageButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot, final OnBookingSelectedListener listener) {

            booking = snapshot.toObject(Booking.class);
            Resources resources = itemView.getResources();

            bookingNoView.setText(booking.getBookingNo());
            bookingNameView.setText(booking.getName());
            bookingContactNoView.setText(booking.getContactNo());

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onBookingSelected(booking);
                    }
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    // Delete the booking
                    final Entity entity = KyooApp.getInstance(KyooApp.getContext()).getEntity();
                    KyooApp.getApiService().deleteBooking(entity.getId(), mQueue.getId(), booking.getId()).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (response.isSuccessful()) {
                                Snackbar.make(view.getRootView().findViewById(android.R.id.content), "Booking deleted.", Snackbar.LENGTH_LONG).show();
                            } else {
                                // handle request errors depending on status code
                                int statusCode = response.code();
                                Snackbar.make(view.getRootView().findViewById(android.R.id.content), "Unable to delete booking. Status code is " + statusCode, Snackbar.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Snackbar.make(view.getRootView().findViewById(android.R.id.content), "Unable to delete booking.", Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }

        public Booking getBooking() {
            return this.booking;
        }

    }
}
