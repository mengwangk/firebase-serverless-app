package com.leisue.kyoo.ui.adapter;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.R;
import com.leisue.kyoo.model.Booking;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.Queue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Booking queue adapter
 */

public class BookingQueueAdapter extends FirestoreRecyclerAdapter<Booking, BookingQueueAdapter.BookingViewHolder> {

    private static final String TAG = "BookingQueueAdapter";

    public interface OnBookingSelectedListener {
        void onBookingSelected(Booking booking);
    }

    private BookingQueueAdapter.OnBookingSelectedListener listener;
    private Queue queue;
    private RecyclerView recyclerView;

    protected BookingQueueAdapter(FragmentActivity activity, Queue queue, Query query, BookingQueueAdapter.OnBookingSelectedListener listener, RecyclerView recyclerView) {
        super(new FirestoreRecyclerOptions.Builder<Booking>()
            .setQuery(query, Booking.class)
            .setLifecycleOwner(activity)
            .build());

        this.queue = queue;
        this.listener = listener;
        this.recyclerView = recyclerView;
    }

    @Override
    public BookingQueueAdapter.BookingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new BookingQueueAdapter.BookingViewHolder(inflater.inflate(R.layout.item_booking, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull BookingViewHolder holder, int position, @NonNull Booking model) {
        holder.bind(model, listener);
    }


    public class BookingViewHolder extends RecyclerView.ViewHolder {

        Booking booking;

        @BindView(R.id.booking_no)
        TextView bookingNoView;

        @BindView(R.id.booking_name)
        TextView bookingNameView;

        @BindView(R.id.booking_contact_no)
        TextView bookingContactNoView;

        BookingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(final Booking booking, final BookingQueueAdapter.OnBookingSelectedListener listener) {
            final Resources resources = itemView.getResources();

            this.booking = booking;
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
        }

        @OnClick(R.id.button_remove)
        void onRemoveBooking(){
            Log.i(TAG, "Delete the history");
            changeBooking(Booking.ACTION.REMOVE);

        }

        @OnClick(R.id.button_done)
        void onDoneBooking(){
            Log.i(TAG, "Done with the history");
            changeBooking(Booking.ACTION.DONE);
        }


        void changeBooking(final Booking.ACTION action) {
            final Entity entity = KyooApp.getInstance(KyooApp.getContext()).getEntity();
            KyooApp.getApiService().deleteBooking(entity.getId(), queue.getId(), booking.getId(), action.getId()).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        Snackbar.make(recyclerView.getRootView(), R.string.message_booking_deleted, Snackbar.LENGTH_LONG).show();
                    } else {
                        // handle request errors depending on status code
                        int statusCode = response.code();
                        Snackbar.make(recyclerView.getRootView(), KyooApp.getContext().getString(R.string.message_booking_delete_status_code, statusCode) , Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Snackbar.make(recyclerView.getRootView(), R.string.message_booking_delete_error, Snackbar.LENGTH_LONG).show();
                }
            });
        }

        public Booking getBooking() {
            return this.booking;
        }

    }
}
