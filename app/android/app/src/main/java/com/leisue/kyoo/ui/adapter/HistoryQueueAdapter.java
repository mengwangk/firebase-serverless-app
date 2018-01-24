package com.leisue.kyoo.ui.adapter;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.leisue.kyoo.R;
import com.leisue.kyoo.model.History;
import com.leisue.kyoo.model.Queue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * History queue adapter.
 */

public class HistoryQueueAdapter extends FirestoreRecyclerAdapter<History, HistoryQueueAdapter.HistoryViewHolder> {

    private static final String TAG = "BookingQueueAdapter";

    public interface OnHistorySelectedListener {
        void onHistorySelected(History booking);
    }

    private OnHistorySelectedListener listener;
    private Queue queue;
    private RecyclerView recyclerView;

    protected HistoryQueueAdapter(FragmentActivity activity, Queue queue, Query query, OnHistorySelectedListener listener, RecyclerView recyclerView) {
        super(new FirestoreRecyclerOptions.Builder<History>()
            .setQuery(query, History.class)
            .setLifecycleOwner(activity)
            .build());

        this.queue = queue;
        this.listener = listener;
        this.recyclerView = recyclerView;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new HistoryViewHolder(inflater.inflate(R.layout.item_booking, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull HistoryViewHolder holder, int position, @NonNull History model) {
        holder.bind(model, listener);
    }


    public class HistoryViewHolder extends RecyclerView.ViewHolder {

        History history;

        @BindView(R.id.booking_no)
        TextView bookingNoView;

        @BindView(R.id.booking_name)
        TextView bookingNameView;

        @BindView(R.id.booking_contact_no)
        TextView bookingContactNoView;

        @BindView(R.id.button_remove)
        ImageButton removeButon;

        HistoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(final History history, final OnHistorySelectedListener listener) {
            final Resources resources = itemView.getResources();

            this.history = history;
            bookingNoView.setText(history.getBookingNo());
            bookingNameView.setText(history.getName());
            bookingContactNoView.setText(history.getContactNo());

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onHistorySelected(history);
                    }
                }
            });
        }

        @OnClick(R.id.button_remove)
        void onRemoveBooking(){
            Log.i(TAG, "Delete the history");
            //changeBooking(Booking.ACTION.REMOVE);

        }

        @OnClick(R.id.button_done)
        void onDoneBooking(){
            Log.i(TAG, "Done with the history");
           // changeBooking(Booking.ACTION.DONE);
        }

        /*
        void changeBooking(final Booking.ACTION action) {
            final Entity entity = KyooApp.getInstance(KyooApp.getContext()).getEntity();
            KyooApp.getApiService().deleteBooking(entity.getId(), queue.getId(), history.getId(), action.getId()).enqueue(new Callback<String>() {
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
        */

        public History getHistory() {
            return this.history;
        }

    }
}
