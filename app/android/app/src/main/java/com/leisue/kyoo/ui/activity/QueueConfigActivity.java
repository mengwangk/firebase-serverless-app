package com.leisue.kyoo.ui.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.R;
import com.leisue.kyoo.ui.adapter.QueueAdapter;
import com.leisue.kyoo.ui.fragment.QueueDialogFragment;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.Queue;
import com.leisue.kyoo.model.QueueRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Queue setup and configuration activity.
 */

public class QueueConfigActivity extends BaseActivity implements QueueAdapter.OnQueueSelectedListener, QueueDialogFragment.QueueListener{

    private static final String TAG = "QueueConfigActivity";

    @BindView(R.id.recycler_queues)
    RecyclerView queuesRecycler;

    @BindView(R.id.view_empty)
    ViewGroup emptyView;

    @BindView(R.id.button_add_queue)
    FloatingActionButton addQueueButton;

    QueueAdapter adapter;

    QueueDialogFragment queueDialog;

    List<Queue> queues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_config);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        queues = new ArrayList<>(0);
        adapter = new QueueAdapter(queues, QueueConfigActivity.this, queuesRecycler);
        queuesRecycler.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isSignedIn()) {
           loadQueues();
        }
    }

    void loadQueues(){
        queuesRecycler.setLayoutManager(new LinearLayoutManager(this));
        queuesRecycler.setItemAnimator(new DefaultItemAnimator());

        final Entity entity = KyooApp.getInstance(this).getEntity();
        KyooApp.getApiService().getQueues(entity.getId()).enqueue(new Callback<List<Queue>>() {
            @Override
            public void onResponse(Call<List<Queue>> call, Response<List<Queue>> response) {
                if (response.isSuccessful()) {
                    final List<Queue> queues = response.body();
                    if (queues == null || queues.size() == 0) {
                        queuesRecycler.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        queuesRecycler.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                        showQueues(queues);
                    }

                } else {
                    // handle request errors depending on status code
                    int statusCode = response.code();
                    Snackbar.make(findViewById(android.R.id.content),  getString(R.string.message_queue_load_status_code, statusCode), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Queue>> call, Throwable t) {
                Log.e(TAG, "Unable to load entity", t);
                Snackbar.make(findViewById(android.R.id.content), R.string.message_queue_load_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    void showQueues(List<Queue> queues) {
        // Sort the queue by name
        Collections.sort(queues);

        this.queues.clear();
        this.queues.addAll(queues);
        adapter.notifyDataSetChanged();
    }

    @OnClick(R.id.button_add_queue)
    public void onAddQueueClicked(View view) {
        Log.i(TAG, "Add a queue");
        queueDialog = new QueueDialogFragment();
        queueDialog.setQueueListener(this);
        queueDialog.show(this.getSupportFragmentManager(), TAG);
    }

    @Override
    public void onQueueSelected(Queue queue) {
        // When a queue is selected
        queueDialog = new QueueDialogFragment();
        queueDialog.setQueueListener(this);
        queueDialog.setQueue(queue);
        queueDialog.show(this.getSupportFragmentManager(), TAG);
    }

    @Override
    public void onQueue(Queue queue) {
        // When a new queue is created or update
        Log.i(TAG, "Queue is added or edited");
        if (TextUtils.isEmpty(queue.getId())) {
            // Add queue
            createQueue(queue);
        } else {
            // Update queue
            updateQueue(queue);
        }
    }

    void createQueue(Queue queue) {
        final Entity entity = KyooApp.getInstance(this).getEntity();
        KyooApp.getApiService().createQueue(entity.getId(), new QueueRequest(queue)).enqueue(new Callback<Queue>() {
            @Override
            public void onResponse(Call<Queue> call, Response<Queue> response) {
                if (response.isSuccessful()) {
                    final Queue queue = response.body();
                    Snackbar.make(findViewById(android.R.id.content), R.string.message_queue_configured, Snackbar.LENGTH_LONG).show();
                    loadQueues();
                } else {
                    // handle request errors depending on status code
                    int statusCode = response.code();
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.message_queue_configure_status_code, statusCode), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Queue> call, Throwable t) {
                Log.e(TAG, "Unable to create queue", t);
                Snackbar.make(findViewById(android.R.id.content), R.string.message_queue_configure_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    void updateQueue(Queue queue) {
        final Entity entity = KyooApp.getInstance(this).getEntity();
        KyooApp.getApiService().updateQueue(entity.getId(), queue.getId(), new QueueRequest(queue)).enqueue(new Callback<Queue>() {
            @Override
            public void onResponse(Call<Queue> call, Response<Queue> response) {
                if (response.isSuccessful()) {
                    final Queue queue = response.body();
                    Snackbar.make(findViewById(android.R.id.content), R.string.message_queue_configured, Snackbar.LENGTH_LONG).show();
                    loadQueues();
                } else {
                    // handle request errors depending on status code
                    int statusCode = response.code();
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.message_queue_configure_status_code, statusCode), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Queue> call, Throwable t) {
                Log.e(TAG, "Unable to create queue", t);
                Snackbar.make(findViewById(android.R.id.content), R.string.message_queue_configure_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
