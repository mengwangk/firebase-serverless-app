package com.leisue.kyoo.ui.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.leisue.kyoo.R;
import com.leisue.kyoo.commons.FirestoreUtils;
import com.leisue.kyoo.databinding.ActivityQueueListBinding;
import com.leisue.kyoo.ui.adapter.QueueListAdapter;
import com.leisue.kyoo.ui.viewmodel.QueueListActivityModel;

/**
 * Queue list activity.
 */

public class QueueListActivity extends AppCompatActivity implements QueueListAdapter.OnQueueSelectedListener {

    private static final String TAG = "QueueListActivity";

    private QueueListAdapter queueListAdapter;

    private QueueListActivityModel viewModel;

    private Query queueListQuery;

    ActivityQueueListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_queue_list);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewModel = ViewModelProviders.of(this).get(QueueListActivityModel.class);

        // Initialize Firestore and the main RecyclerView
        initFirestore();
        initRecyclerView();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Start listening for Firestore updates
        if (queueListAdapter != null) {
            queueListAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (queueListAdapter != null) {
            queueListAdapter.stopListening();
        }
    }

    private void initFirestore() {
        this.queueListQuery = FirestoreUtils.getQueryForQueueList();
    }

    private void initRecyclerView() {
        queueListAdapter = new QueueListAdapter(this.queueListQuery, this) {
            @Override
            protected void onDataChanged() {
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
            protected void onError(FirebaseFirestoreException e) {
                Snackbar.make(binding.getRoot(), "Unable to load queue list.", Snackbar.LENGTH_LONG).show();
            }
        };

        binding.recyclerQueues.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerQueues.setAdapter(queueListAdapter);
    }

    @Override
    public void onQueueSelected(DocumentSnapshot queue) {

    }

}
