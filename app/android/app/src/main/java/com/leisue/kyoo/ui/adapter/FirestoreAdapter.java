package com.leisue.kyoo.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * RecyclerView adapter for displaying the results of a Firestore {@link Query}.
 * <p>
 * Note that this class forgoes some efficiency to gain simplicity. For example, the result of
 * {@link DocumentSnapshot#toObject(Class)} is not cached so the same object may be deserialized
 * many times as the user scrolls.
 */
public abstract class FirestoreAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements EventListener<QuerySnapshot> {

    private static final String TAG = "FirestoreAdapter";

    private Query query;
    private ListenerRegistration registration;

    private ArrayList<DocumentSnapshot> snapshots = new ArrayList<>();

    public FirestoreAdapter(Query query) {
        this.query = query;
    }

    @Override
    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "onEvent:error", e);
            onError(e);
            return;
        }

        // Dispatch the event
        Log.d(TAG, "onEvent:numChanges:" + documentSnapshots.getDocumentChanges().size());
        for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
            switch (change.getType()) {
                case ADDED:
                    onDocumentAdded(change);
                    break;
                case MODIFIED:
                    onDocumentModified(change);
                    break;
                case REMOVED:
                    onDocumentRemoved(change);
                    break;
            }
        }

        onDataChanged();
    }

    public void startListening() {
        if (query != null && registration == null) {
            registration = query.addSnapshotListener(this);
        }
    }

    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }

        snapshots.clear();
        notifyDataSetChanged();
    }

    public void setQuery(Query query) {
        // Stop listening
        stopListening();

        // Clear existing data
        snapshots.clear();
        notifyDataSetChanged();

        // Listen to new query
        this.query = query;
        startListening();
    }

    @Override
    public int getItemCount() {
        return snapshots.size();
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return snapshots.get(index);
    }

    protected void onDocumentAdded(DocumentChange change) {
        snapshots.add(change.getNewIndex(), change.getDocument());
        notifyItemInserted(change.getNewIndex());
    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            snapshots.set(change.getOldIndex(), change.getDocument());
            notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            snapshots.remove(change.getOldIndex());
            snapshots.add(change.getNewIndex(), change.getDocument());
            notifyItemMoved(change.getOldIndex(), change.getNewIndex());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        snapshots.remove(change.getOldIndex());
        notifyItemRemoved(change.getOldIndex());
    }

    protected void onError(FirebaseFirestoreException e) {
    }

    ;

    protected void onDataChanged() {
    }
}
