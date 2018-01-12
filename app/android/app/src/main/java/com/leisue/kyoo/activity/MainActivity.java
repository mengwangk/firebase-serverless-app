package com.leisue.kyoo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.KyooConfig;
import com.leisue.kyoo.R;
import com.leisue.kyoo.fragment.QueueFragment;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.Queue;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

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
    protected void onStart() {
        super.onStart();

        if (isSignedIn()) {
            // Already signed in, load the entity and its queues
            loadEntity();
        }
    }

    private void setupQueues(final List<Queue> queues) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        for (Queue queue : queues) {
            adapter.addFrag(QueueFragment.newInstance(queue), queue.getName());
        }
        viewPager.setAdapter(adapter);
    }

    private void loadEntity() {
        KyooApp.getApiService().getEntity(KyooConfig.ENTITY_ID).enqueue(new Callback<Entity>() {
            @Override
            public void onResponse(Call<Entity> call, Response<Entity> response) {
                if (response.isSuccessful()) {
                    final Entity entity = response.body();
                    KyooApp.getInstance(MainActivity.this).setEntity(entity);  // Set the returned entity into global application context
                    setHeaderView();
                    loadQueues(entity);
                } else {
                    // handle request errors depending on status code
                    int statusCode = response.code();
                    Snackbar.make(findViewById(android.R.id.content), "Unable to load entity. Status code is " + statusCode, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Entity> call, Throwable t) {
                Log.e(TAG, "Unable to load entity", t);
                Snackbar.make(findViewById(android.R.id.content), "Unable to load entity", Snackbar.LENGTH_LONG).show();
            }
        });
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
                    Snackbar.make(findViewById(android.R.id.content), "Unable to load queue. Status code is " + statusCode, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Queue>> call, Throwable t) {
                Log.e(TAG, "Unable to load queues", t);
                Snackbar.make(findViewById(android.R.id.content), "Unable to load queues", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Highlight the menu item
        navigationView.setCheckedItem(R.id.menu_item_queue);
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
