package com.leisue.kyoo.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.leisue.kyoo.R;
import com.leisue.kyoo.model.Queue;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Queue dialog
 */

public class QueueDialogFragment extends DialogFragment {

    public static final String TAG = "QueueDialog";

    private Queue queue;

    public interface QueueListener {
        void onQueue(Queue queue);
    }

    private View rootView;

    @BindView(R.id.edit_queue_name)
    EditText queueName;

    @BindView(R.id.edit_queue_min_capacity)
    EditText queueMinCapacity;

    @BindView(R.id.edit_queue_max_capacity)
    EditText queueMaxCapacity;

    @BindView(R.id.edit_queue_prefix)
    EditText queuePrefix;

    private QueueDialogFragment.QueueListener queueListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_queue, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.queue != null) {
            queueName.setText(queue.getName());
            queueMinCapacity.setText(queue.getMinCapacity().toString());
            queueMaxCapacity.setText(queue.getMaxCapacity().toString());
            queuePrefix.setText(queue.getPrefix());
        } else {
            queuePrefix.setText(randomPrefix());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @OnClick(R.id.button_apply)
    public void onAddClicked() {
        if (queueListener != null) {
            queueListener.onQueue(getQueue());
        }

        dismiss();
    }

    @OnClick(R.id.button_cancel)
    public void onCancelClicked() {
        dismiss();
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }
    public Queue getQueue() {
        if (rootView != null) {
            if (this.queue == null) this.queue = new Queue();
            queue.setName(queueName.getText().toString());
            queue.setMinCapacity(Integer.parseInt(queueMinCapacity.getText().toString()));
            queue.setMaxCapacity(Integer.parseInt(queueMaxCapacity.getText().toString()));
            queue.setPrefix(queuePrefix.getText().toString());
        }

        return queue;
    }

    public void setQueueListener(QueueDialogFragment.QueueListener listener) {
        this.queueListener = listener;
    }

    String randomPrefix(){
        Random r = new Random();
        return String.valueOf((char)(r.nextInt(26) + 'A'));
    }
}
