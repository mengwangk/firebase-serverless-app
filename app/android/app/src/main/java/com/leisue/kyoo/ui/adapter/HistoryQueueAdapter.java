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
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.History;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * History queue adapter.
 */

public class HistoryQueueAdapter extends FirestoreRecyclerAdapter<History, HistoryQueueAdapter.HistoryViewHolder> {

    private static final String TAG = "HistoryQueueAdapter";

    public interface OnHistorySelectedListener {
        void onHistorySelected(History history);
    }

    private OnHistorySelectedListener listener;
    private RecyclerView recyclerView;

    protected HistoryQueueAdapter(FragmentActivity activity, Query query, OnHistorySelectedListener listener, RecyclerView recyclerView) {
        super(new FirestoreRecyclerOptions.Builder<History>()
            .setQuery(query, History.class)
            .setLifecycleOwner(activity)
            .build());

        this.listener = listener;
        this.recyclerView = recyclerView;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new HistoryViewHolder(inflater.inflate(R.layout.item_history, parent, false));
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

        @BindView(R.id.booking_status)
        TextView bookingStatus;

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
            bookingStatus.setText(history.getStatus());

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


        @OnClick(R.id.button_return)
        void onReturnBooking() {
            Log.i(TAG, "Return the history");
            returnBooking(History.ACTION.RETURN);
        }

        void returnBooking(final History.ACTION action) {
            final Entity entity = KyooApp.getInstance(KyooApp.getContext()).getEntity();

            KyooApp.getApiService().returnHistory(entity.getId(), history.getQueueId(), history.getId(), action.getId()).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        Snackbar.make(recyclerView.getRootView(), R.string.message_history_updated, Snackbar.LENGTH_LONG).show();
                    } else {
                        // handle request errors depending on status code
                        int statusCode = response.code();
                        Snackbar.make(recyclerView.getRootView(), KyooApp.getContext().getString(R.string.message_history_update_error_status_code, statusCode), Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Snackbar.make(recyclerView.getRootView(), R.string.message_history_update_error, Snackbar.LENGTH_LONG).show();
                }
            });
        }

        public History getHistory() {
            return this.history;
        }

    }
}
