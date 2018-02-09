package com.leisue.kyoo.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.R;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.History;
import com.leisue.kyoo.model.Queue;
import com.leisue.kyoo.ui.fragment.BookingQueueFragment;
import com.leisue.kyoo.ui.fragment.HistoryQueueFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements BaseActivity.OnEntityLoadedListener{

    private static final String TAG = "MainActivity";

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_queue, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_queue_list:
                startActivity(new Intent(this, QueueListActivity.class));
                return true;
            case R.id.action_save_to_archive:
                saveToArchive();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isSignedIn()) {
            // Already signed in, load the entity and its queues
            setEntityListener(this);
            loadEntity();
        }
    }


    @Override
    public void onEntityLoaded(Entity entity) {
        loadQueues(entity);
        KyooApp.getInstance(this).setEntity(entity);  // Set the returned entity into global application context
        setHeaderView();

        //
        // Testing
        //launchActivity(OfflineSyncActivity.class);
    }

    private void setupQueues(final List<Queue> queues) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        if (queues.size() == 0) {
            Snackbar.make(findViewById(android.R.id.content), R.string.message_configure_queue, Snackbar.LENGTH_LONG).show();
            launchActivity(SettingsActivity.class);
        } else {
            for (Queue queue : queues) {
                adapter.addFrag(BookingQueueFragment.newInstance(queue), queue.getName());
            }
            // Add the history queue
            adapter.addFrag(HistoryQueueFragment.newInstance(), HistoryQueueFragment.TAG);
            viewPager.setAdapter(adapter);
        }
    }

    private void loadQueues(final Entity entity) {
        KyooApp.getApiService().getQueues(entity.getId()).enqueue(new Callback<List<Queue>>() {
            @Override
            public void onResponse(Call<List<Queue>> call, Response<List<Queue>> response) {
                if (response.isSuccessful()) {
                    final List<Queue> queues = response.body();
                    setupQueues(queues);
                } else {
                    // handle request errors depending on status code
                    int statusCode = response.code();
                    Snackbar.make(findViewById(android.R.id.content),  getString(R.string.message_queue_load_status_code, statusCode), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Queue>> call, Throwable t) {
                Log.e(TAG, "Unable to load queues", t);
                Snackbar.make(findViewById(android.R.id.content), R.string.message_queue_load_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Highlight the menu item
        navigationView.setCheckedItem(R.id.menu_item_queue);
    }

    void saveToArchive(){
        final Entity entity = KyooApp.getInstance(this).getEntity();
        KyooApp.getApiService().archiveHistory(entity.getId(), History.ACTION.ARCHIVE.getId()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Snackbar.make(findViewById(android.R.id.content), R.string.message_history_archived, Snackbar.LENGTH_LONG).show();
                } else {
                    // handle request errors depending on status code
                    int statusCode = response.code();
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.message_history_archive_error_status_code, statusCode), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Snackbar.make(findViewById(android.R.id.content), R.string.message_history_archive_error, Snackbar.LENGTH_LONG).show();
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
