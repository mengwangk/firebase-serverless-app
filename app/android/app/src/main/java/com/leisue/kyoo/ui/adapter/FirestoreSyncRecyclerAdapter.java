package com.leisue.kyoo.ui.adapter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.ChangeEventListener;
import com.firebase.ui.firestore.FirestoreArray;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.leisue.kyoo.dao.IDao;

/**
 * RecyclerView adapter that listens to a {@link FirestoreArray} and displays its data in real
 * time.
 *
 * @param <T>  model class, for parsing {@link DocumentSnapshot}s.
 * @param <VH> {@link RecyclerView.ViewHolder} class.
 */
public abstract class FirestoreSyncRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements ChangeEventListener, LifecycleObserver {

    private static final String TAG = "FirestoreSyncAdapter";

    private final ObservableSnapshotArray<T> mSnapshots;

    Class<T> clazz;


    private IDao<T> dao;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     */
    public FirestoreSyncRecyclerAdapter(@NonNull FirestoreRecyclerOptions<T> options, IDao<T> dao, Class<T> clazz) {
        mSnapshots = options.getSnapshots();
        this.dao = dao;
        this.clazz = clazz;

        Log.i(TAG, "Snapshot size = " + mSnapshots.size());

        if (options.getOwner() != null) {
            options.getOwner().getLifecycle().addObserver(this);
        }
    }

    /**
     * Start listening for database changes and populate the adapter.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void startListening() {
        if (!mSnapshots.isListening(this)) {
            mSnapshots.addChangeEventListener(this);
        }
    }

    /**
     * Stop listening for database changes and clear all items in the adapter.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void stopListening() {
        mSnapshots.removeChangeEventListener(this);
        notifyDataSetChanged();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void cleanup(LifecycleOwner source) {
        source.getLifecycle().removeObserver(this);
    }

    /**
     * Returns the backing {@link ObservableSnapshotArray} used to populate this adapter.
     *
     * @return the backing snapshot array
     */
    @NonNull
    public ObservableSnapshotArray<T> getSnapshots() {
        return mSnapshots;
    }

    /**
     * Gets the item at the specified position from the backing snapshot array.
     *
     * @see ObservableSnapshotArray#get(int)
     */
    @NonNull
    public T getItem(int position) {
        T a = dao.getRow(position);
        return dao.getRow(position);
    }
    /*
    public T getItem(int position) {
        return mSnapshots.get(position);
    }
    */

    @Override
    public int getItemCount() {
        int count = dao.count();
        return dao.count();
    }
    /*
    public int getItemCount() {
        return mSnapshots.isListening(this) ? mSnapshots.size() : 0;
    }
    */

    @Override
    public void onChildChanged(@NonNull ChangeEventType type,
                               @NonNull DocumentSnapshot snapshot,
                               int newIndex,
                               int oldIndex) {
        T obj = snapshot.toObject(clazz);
        switch (type) {
            case ADDED:
                dao.insert(obj);
                //notifyItemInserted(newIndex);
                break;
            case CHANGED:
                dao.update(obj);
                //notifyItemChanged(newIndex);
                break;
            case REMOVED:
                dao.delete(obj);
               // notifyItemRemoved(oldIndex);
                break;
            case MOVED:
                dao.delete(obj);
                //notifyItemMoved(oldIndex, newIndex);
                break;
            default:
                throw new IllegalStateException("Incomplete case statement");
        }
        notifyDataSetChanged();
    }

    @Override
    public void onDataChanged() {
    }

    @Override
    public void onError(@NonNull FirebaseFirestoreException e) {
        Log.w(TAG, "onError", e);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        onBindViewHolder(holder, position, getItem(position));
    }

    /**
     * @param model the model object containing the data that should be used to populate the view.
     * @see #onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    protected abstract void onBindViewHolder(@NonNull VH holder, int position, @NonNull T model);
}
