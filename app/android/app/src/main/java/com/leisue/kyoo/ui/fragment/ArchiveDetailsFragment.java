package com.leisue.kyoo.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leisue.kyoo.R;
import com.leisue.kyoo.model.ArchiveDetails;
import com.leisue.kyoo.model.Queue;
import com.leisue.kyoo.ui.activity.QueueConfigActivity;
import com.leisue.kyoo.ui.adapter.ArchiveDetailsAdapter;
import com.leisue.kyoo.ui.adapter.QueueAdapter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Archive details fragment
 */

public class ArchiveDetailsFragment extends Fragment {

    private static final String TAG = "ArchiveDetailsFragment";

    private static final String ARCHIVE_BUNDLE_KEY = "archives";

    @BindView(R.id.recycler_archive_details)
    RecyclerView archiveDetailsRecycler;

    @BindView(R.id.view_empty)
    ViewGroup emptyView;

    private ArchiveDetailsAdapter adapter;

    private List<ArchiveDetails> details;

    public static ArchiveDetailsFragment newInstance(final List<ArchiveDetails> archiveDetailsList) {

        ArchiveDetailsFragment fragment = new ArchiveDetailsFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString(ARCHIVE_BUNDLE_KEY, new Gson().toJson(archiveDetailsList));  // This is just to make it simple to demo. Use a better way if possible
        fragment.setArguments(bundle);
        return fragment;
    }

    public ArchiveDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String archives = getArguments().getString(ARCHIVE_BUNDLE_KEY);
        Type listType = new TypeToken<ArrayList<ArchiveDetails>>(){}.getType();
        this.details = new Gson().fromJson(archives, listType);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_archive_details, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        adapter = new ArchiveDetailsAdapter(this.details, archiveDetailsRecycler);
        archiveDetailsRecycler.setAdapter(adapter);

        archiveDetailsRecycler.setLayoutManager(new LinearLayoutManager(this.getContext()));
        archiveDetailsRecycler.setItemAnimator(new DefaultItemAnimator());
        archiveDetailsRecycler.setAdapter(adapter);

        super.onViewCreated(view, savedInstanceState);
    }


}

