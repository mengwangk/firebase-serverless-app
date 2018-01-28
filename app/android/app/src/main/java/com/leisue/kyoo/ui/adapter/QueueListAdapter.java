package com.leisue.kyoo.ui.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.leisue.kyoo.commons.FirestoreUtils;
import com.leisue.kyoo.databinding.ItemQueueListBinding;
import com.leisue.kyoo.model.Booking;
import com.leisue.kyoo.model.Queue;

/**
 * Queue list adapter
 */

public class QueueListAdapter extends FirestoreAdapter<QueueListAdapter.QueueListViewHolder> {

    private static final String TAG = "QueueListAdapter";

    public interface OnQueueSelectedListener {
        void onQueueSelected(DocumentSnapshot restaurant);
    }

    private OnQueueSelectedListener listener;

    public QueueListAdapter(Query query, OnQueueSelectedListener listener) {
        super(query);
        this.listener = listener;
    }

    @Override
    public QueueListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemQueueListBinding itemBinding = ItemQueueListBinding.inflate(inflater, parent, false);
        return new QueueListViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(QueueListViewHolder holder, int position) {
        holder.bind(getSnapshot(position), listener);
    }


    @Override
    public void onViewRecycled(QueueListViewHolder holder) {
        super.onViewRecycled(holder);
    }


    @Override
    public void onViewDetachedFromWindow(QueueListViewHolder holder) {
        // Stop listening for changes
        holder.stopListening();
        super.onViewDetachedFromWindow(holder);
    }

    class QueueListViewHolder extends RecyclerView.ViewHolder implements EventListener<QuerySnapshot> {

        private final ItemQueueListBinding binding;

        private Queue queue;
        private Query queueQuery;
        private ListenerRegistration queueListener;

        public QueueListViewHolder(ItemQueueListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final DocumentSnapshot snapshot, final OnQueueSelectedListener listener) {
            queue = snapshot.toObject(Queue.class);
            binding.queueName.setText(queue.getName());
            binding.queueCapacity.setText(queue.getMinCapacity() + " to " + queue.getMaxCapacity());

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onQueueSelected(snapshot);
                    }
                }
            });

            startListening();
        }

        void startListening() {
            if (queueQuery == null && queueListener == null) {
                queueQuery = FirestoreUtils.getQueryForLatestBooking(queue);
                queueListener = queueQuery.addSnapshotListener(this);
            }
        }

        public void stopListening() {
            if (queueQuery != null && queueListener != null) {
                queueListener.remove();
                queueListener = null;
                queueQuery = null;
            }
        }

        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null) {
                Log.w(TAG, "onEvent:error", e);
                onError(e);
                return;
            }
            // Dispatch the event - currently we are only interested in ADD
            for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
                switch (change.getType()) {
                    case ADDED:
                        final Booking booking = change.getDocument().toObject(Booking.class);
                        binding.queueCurrentNo.setText(booking.getBookingNo());
                        binding.queueCurrentSeats.setText(String.valueOf(booking.getNoOfSeats()));
                        break;
                    case MODIFIED:

                        break;
                    case REMOVED:

                        break;
                }
            }
        }
    }
}
