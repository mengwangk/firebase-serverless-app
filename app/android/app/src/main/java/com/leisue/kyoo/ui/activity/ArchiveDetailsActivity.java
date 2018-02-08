package com.leisue.kyoo.ui.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.R;
import com.leisue.kyoo.commons.FirestoreUtils;
import com.leisue.kyoo.model.ArchiveDetails;
import com.leisue.kyoo.model.ArchiveSummary;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.Queue;
import com.leisue.kyoo.ui.adapter.ArchiveSummaryAdapter;
import com.leisue.kyoo.ui.fragment.ArchiveDetailsFragment;
import com.leisue.kyoo.ui.fragment.BookingQueueFragment;
import com.leisue.kyoo.ui.fragment.HistoryQueueFragment;
import com.leisue.kyoo.ui.viewmodel.ArchiveActivityModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Archive details activity.
 */

public class ArchiveDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ArchiveDetailsActivity";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    Query query;

    // This list contain all records
    List<ArchiveDetails> archiveDetailsList = new ArrayList<>(10);

    Map<String, List<ArchiveDetails>> archiveQueueMap = new HashMap<>(10);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tabLayout.setupWithViewPager(viewPager);

        // Initialize Firestore and the main RecyclerView
        initFirestore();
    }


    private void initFirestore() {
        String archiveId = getIntent().getStringExtra("archive_id");
        this.query = FirestoreUtils.getQueryForArchiveDetails(archiveId);
        query.get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            ArchiveDetails archiveDetails = document.toObject(ArchiveDetails.class);
                            archiveDetailsList.add(archiveDetails);
                            if (archiveQueueMap.containsKey(archiveDetails.getQueueName())) {
                                List<ArchiveDetails> details = archiveQueueMap.get(archiveDetails.getQueueName());
                                details.add(archiveDetails);
                            } else {
                                List<ArchiveDetails> details = new ArrayList<>(1);
                                details.add(archiveDetails);
                                archiveQueueMap.put(archiveDetails.getQueueName(), details);
                            }
                        }
                        Log.i(TAG, "Total records: " + archiveDetailsList.size());
                        Log.i(TAG, "Total queues: " + archiveQueueMap.size());

                        try {
                            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

                            for (String queueName : archiveQueueMap.keySet()) {
                                List<ArchiveDetails> details = archiveQueueMap.get(queueName);
                                adapter.addFrag(ArchiveDetailsFragment.newInstance(details), queueName + " (" + details.size() + ")");
                            }
                            // Add the history queue
                            adapter.addFrag(ArchiveDetailsFragment.newInstance(archiveDetailsList), "All (" + archiveDetailsList.size() + ")");
                            viewPager.setAdapter(adapter);
                        } catch (Exception e) {
                            Log.e(TAG, "error", e);
                        }


                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> fragmentTitles = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        void addFrag(Fragment fragment, String title) {
            fragments.add(fragment);
            fragmentTitles.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }
    }

}
