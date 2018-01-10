package com.starsystems.kyoo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.starsystems.kyoo.R;
import com.starsystems.kyoo.model.Queue;


public class QueueFragment extends Fragment {

    private static final String QUEUE = "queue";

    public static QueueFragment newInstance(final Queue queue) {
        QueueFragment queueFragment = new QueueFragment();
        Bundle bundle = new Bundle(1);
        bundle.putSerializable(QUEUE, queue);
        queueFragment.setArguments(bundle);
        return queueFragment;
    }

    private Queue queue;

    public QueueFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.queue = (Queue)getArguments().getSerializable(QUEUE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_queue, container, false);
    }

}
