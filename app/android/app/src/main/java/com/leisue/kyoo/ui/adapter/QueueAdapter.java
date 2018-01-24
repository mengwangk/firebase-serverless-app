package com.leisue.kyoo.ui.adapter;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.R;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.Queue;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Queue adapter.
 */

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {

    public interface OnQueueSelectedListener {
        void onQueueSelected(Queue queue);
    }

    private QueueAdapter.OnQueueSelectedListener listener;

    private List<Queue> queues;

    private RecyclerView recyclerView;

    public class QueueViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.queue_name)
        TextView queueName;

        @BindView(R.id.button_remove)
        ImageButton deleteButton;

        public QueueViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, this.itemView);
        }

        void bind(final Queue queue, final OnQueueSelectedListener listener) {
            queueName.setText(queue.getName());

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onQueueSelected(queues.get(getAdapterPosition()));
                    }
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    // Delete the queue
                    final Entity entity = KyooApp.getInstance(KyooApp.getContext()).getEntity();
                    KyooApp.getApiService().deleteQueue(entity.getId(), queue.getId()).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (response.isSuccessful()) {
                                Snackbar.make(recyclerView.getRootView(), R.string.message_queue_deleted, Snackbar.LENGTH_LONG).show();
                                queues.remove(queue);
                                notifyDataSetChanged();
                            } else {
                                // handle request errors depending on status code
                                int statusCode = response.code();
                                Snackbar.make(recyclerView.getRootView(), KyooApp.getContext().getString(R.string.message_queue_delete_status_code, statusCode) , Snackbar.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Snackbar.make(recyclerView.getRootView(), R.string.message_queue_delete_error, Snackbar.LENGTH_LONG).show();
                        }
                    });

                }
            });
        }
    }

    public QueueAdapter(List<Queue> queues, OnQueueSelectedListener listener, RecyclerView recyclerView) {
        this.queues = queues;
        this.listener = listener;
        this.recyclerView = recyclerView;
    }

    @Override
    public QueueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_queue, parent, false);
        return new QueueViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(QueueViewHolder holder, int position) {
        Queue queue = queues.get(position);
        holder.bind(queue, listener);
    }

    @Override
    public int getItemCount() {
        return queues.size();
    }
}
