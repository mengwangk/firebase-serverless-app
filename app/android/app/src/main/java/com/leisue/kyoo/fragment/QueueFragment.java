package com.leisue.kyoo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.leisue.kyoo.R;
import com.leisue.kyoo.adapter.QueueAdapter;
import com.leisue.kyoo.model.Queue;
import com.leisue.kyoo.viewmodel.MainActivityViewModel;

import butterknife.BindView;


public class QueueFragment extends Fragment implements QueueAdapter.OnBookingSelectedListener {

    private static final String TAG = "QueueFragment";


    private static final String QUEUE_BUNDLE_KEY = "queue";


    @BindView(R.id.recycler_bookings)
    RecyclerView mBookingsRecycler;

    @BindView(R.id.view_empty)
    ViewGroup mEmptyView;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private QueueAdapter mAdapter;

    public static QueueFragment newInstance(final Queue queue) {
        QueueFragment queueFragment = new QueueFragment();
        Bundle bundle = new Bundle(1);
        bundle.putSerializable(QUEUE_BUNDLE_KEY, queue);
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
        this.queue = (Queue) getArguments().getSerializable(QUEUE_BUNDLE_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_queue, container, false);
    }

    @Override
    public void onBookingSelected(DocumentSnapshot restaurant) {
        // A booking is selected
    }
}
