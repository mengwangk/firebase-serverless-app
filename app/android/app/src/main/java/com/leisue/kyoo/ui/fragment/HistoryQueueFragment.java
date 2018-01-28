package com.leisue.kyoo.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.leisue.kyoo.R;
import com.leisue.kyoo.commons.FirestoreUtils;
import com.leisue.kyoo.model.History;
import com.leisue.kyoo.ui.adapter.HistoryQueueAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Historical booking fragment.
 */

public class HistoryQueueFragment extends Fragment implements HistoryQueueAdapter.OnHistorySelectedListener {

    public static final String TAG = "History";

    @BindView(R.id.recycler_history)
    RecyclerView historyRecycler;

    @BindView(R.id.view_empty)
    ViewGroup emptyView;

    private Query query;
    private HistoryQueueAdapter adapter;

    public static HistoryQueueFragment newInstance() {
        return new HistoryQueueFragment();
    }

    public HistoryQueueFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        query = FirestoreUtils.getQueryForHistoryQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        // RecyclerView
        adapter = new HistoryQueueAdapter(getActivity(), query, this, historyRecycler) {
            @Override
            public void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    historyRecycler.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    historyRecycler.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.message_load_queue_error , Snackbar.LENGTH_LONG).show();
            }
        };

        // historyRecycler.setHasFixedSize(true);
        historyRecycler.setLayoutManager(new LinearLayoutManager(this.getContext()));
        historyRecycler.setItemAnimator(new DefaultItemAnimator());
        historyRecycler.setAdapter(adapter);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }



    @Override
    public void onHistorySelected(History history) {
        Log.i(TAG, "History clicked");
    }
}
