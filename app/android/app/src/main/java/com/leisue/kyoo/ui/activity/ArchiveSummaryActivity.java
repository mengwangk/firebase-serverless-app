package com.leisue.kyoo.ui.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.R;
import com.leisue.kyoo.commons.FirestoreUtils;
import com.leisue.kyoo.model.ArchiveSummary;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.ui.adapter.ArchiveSummaryAdapter;
import com.leisue.kyoo.ui.viewmodel.ArchiveActivityModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Archive activity.
 */

public class ArchiveSummaryActivity extends BaseActivity implements ArchiveSummaryAdapter.OnArchiveSummarySelectedListener {

    private static final String TAG = "ArchiveSummaryActivity";

    ArchiveActivityModel archiveActivityModel;


    @BindView(R.id.recycler_archive_summary)
    RecyclerView archiveSummaryRecycler;

    @BindView(R.id.view_empty)
    ViewGroup emptyView;

    private Query query;
    private ArchiveSummaryAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_summary);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set view model
        archiveActivityModel = ViewModelProviders.of(this).get(ArchiveActivityModel.class);

        // Initialize Firestore and the main RecyclerView
        initFirestore();
        initRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_archive, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_archive_delete:
                deleteArchives();
                return true;
            case R.id.action_archive_filter:
                filterAchives();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        navigationView.setCheckedItem(R.id.menu_item_archives);
    }

    private void initFirestore() {
        this.query = FirestoreUtils.getQueryForArchiveSummary();
    }

    private void initRecyclerView() {
        adapter = new ArchiveSummaryAdapter(this, query, archiveSummaryRecycler, this) {

            @Override
            public void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    archiveSummaryRecycler.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    archiveSummaryRecycler.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content), R.string.message_load_archives_error , Snackbar.LENGTH_LONG).show();
            }
        };

        archiveSummaryRecycler.setLayoutManager(new LinearLayoutManager(this));
        archiveSummaryRecycler.setItemAnimator(new DefaultItemAnimator());
        archiveSummaryRecycler.setAdapter(adapter);
    }

    void deleteArchives(){
        List<String> ids = new ArrayList(1);
        for (int i = 0 ; i < adapter.getItemCount(); i++) {
            ArchiveSummary archiveSummary = adapter.getItem(i);
            if (archiveSummary.isSelected()){
                // Proceed to delete the archive
                ids.add(archiveSummary.getId());
            }
        }

        if (ids.size() > 0) {
            final Entity entity = KyooApp.getInstance(KyooApp.getContext()).getEntity();
            KyooApp.getApiService().deleteArchives(entity.getId(), joinIds(ids)).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        Snackbar.make(findViewById(android.R.id.content), "Archives deleted", Snackbar.LENGTH_LONG).show();
                    } else {
                        // handle request errors depending on status code
                        int statusCode = response.code();
                        Snackbar.make(findViewById(android.R.id.content), "Status code is " + statusCode, Snackbar.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Snackbar.make(findViewById(android.R.id.content), "Unable to delete archives", Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    void filterAchives(){
        Snackbar.make(findViewById(android.R.id.content), "Refer to filterArchives method in ArchiveSummaryAdapter", Snackbar.LENGTH_LONG).show();
        try {
            SimpleDateFormat sf = new SimpleDateFormat("MMM dd, yyyy");
            String from = "Feb 5, 2018";
            String to = "Feb 5, 2018";
            Date fromDate = sf.parse(from);
            Date toDate = sf.parse(to);
            adapter.filter(fromDate, toDate);
        } catch (Exception e){
            Snackbar.make(findViewById(android.R.id.content), "Unable to filter archives", Snackbar.LENGTH_LONG).show();
        }
    }

    String joinIds(List<String> ids) {
        final String SEPARATOR = ",";
        StringBuilder csvBuilder = new StringBuilder();

        for(String city : ids){
            csvBuilder.append(city);
            csvBuilder.append(SEPARATOR);
        }

        String csv = csvBuilder.toString();
        return csv.substring(0, csv.length() - SEPARATOR.length());
    }

    @Override
    public void onArchiveSummarySelected(ArchiveSummary archiveSummary) {
        Log.i(TAG, "Show archive details");
        Intent intent = new Intent(this, ArchiveDetailsActivity.class);
        intent.putExtra("archive_id", archiveSummary.getId());
        startActivity(intent);
    }
}
