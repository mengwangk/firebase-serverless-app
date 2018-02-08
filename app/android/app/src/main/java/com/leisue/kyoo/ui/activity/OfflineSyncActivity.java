package com.leisue.kyoo.ui.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestoreException;
import com.leisue.kyoo.R;
import com.leisue.kyoo.databinding.ActivityOfflineSyncBinding;
import com.leisue.kyoo.ui.adapter.OfflineSyncRecyclerAdapter;

/**
 * Activity to test for offline sync
 */

public class OfflineSyncActivity extends AppCompatActivity {

    private static final String TAG = "OfflineSyncActivity";

    OfflineSyncRecyclerAdapter adapter;
    ActivityOfflineSyncBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_offline_sync);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new OfflineSyncRecyclerAdapter(this) {
            @Override
            public void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    binding.recyclerQueues.setVisibility(View.GONE);
                    binding.viewEmpty.setVisibility(View.VISIBLE);
                } else {
                    binding.recyclerQueues.setVisibility(View.VISIBLE);
                    binding.viewEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(binding.getRoot(), R.string.message_load_queue_error, Snackbar.LENGTH_LONG).show();
            }
        };

        binding.recyclerQueues.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerQueues.setAdapter(adapter);

        binding.btnAddQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Add a new queue randomly");
                Snackbar.make(binding.getRoot(), "Adding a new queue randomly", Snackbar.LENGTH_LONG).show();
            }
        });

    }

}
