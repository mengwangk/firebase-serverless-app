package com.leisue.kyoo.ui.adapter;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.commons.FirestoreUtils;
import com.leisue.kyoo.databinding.ItemOfflineSyncBinding;
import com.leisue.kyoo.model.Queue;

/**
 * Testing for offline sync.
 */

public class OfflineSyncRecyclerAdapter extends FirestoreSyncRecyclerAdapter<Queue, OfflineSyncRecyclerAdapter.OfflineSyncViewHolder> {

    private static final String TAG = "SyncAdapter";

    public OfflineSyncRecyclerAdapter(AppCompatActivity activity){
        super(new FirestoreRecyclerOptions.Builder<Queue>()
            .setQuery(FirestoreUtils.getQueryForQueueList(), Queue.class)
            .setLifecycleOwner(activity)
            .build(), KyooApp.getDatabase().queueDao(), Queue.class);
    }


    @Override
    protected void onBindViewHolder(@NonNull OfflineSyncViewHolder holder, int position, @NonNull Queue model) {
        Log.i(TAG, "Binding model");
        holder.bind(model);
    }

    @Override
    public OfflineSyncViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemOfflineSyncBinding itemBinding = ItemOfflineSyncBinding.inflate(inflater, parent, false);
        return new OfflineSyncRecyclerAdapter.OfflineSyncViewHolder(itemBinding);
    }

    class OfflineSyncViewHolder extends RecyclerView.ViewHolder  {

        private final ItemOfflineSyncBinding binding;

        private Queue queue;

        public OfflineSyncViewHolder(ItemOfflineSyncBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Queue queue) {
            final Resources resources = itemView.getResources();
            this.queue = queue;
            binding.queueName.setText(queue.getName());
            binding.queueCapacity.setText(queue.getMinCapacity() + " to " + queue.getMaxCapacity());
            binding.queuePrefix.setText(queue.getPrefix());
        }
    }
}
