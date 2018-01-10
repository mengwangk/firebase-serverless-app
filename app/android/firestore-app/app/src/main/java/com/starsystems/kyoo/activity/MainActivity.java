package com.starsystems.kyoo.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.starsystems.kyoo.AppConfig;
import com.starsystems.kyoo.R;
import com.starsystems.kyoo.fragment.QueueFragment;
import com.starsystems.kyoo.model.Entity;
import com.starsystems.kyoo.model.Queue;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        loadEntity();
        loadQueues();
    }

    private void setupQueues(final List<Queue> queues) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        for (Queue queue : queues) {
            adapter.addFrag(QueueFragment.newInstance(queue), queue.getName());
        }
        viewPager.setAdapter(adapter);
    }

    private void loadEntity() {
        AppConfig.getApiService().getEntity(AppConfig.ENTITY_ID).enqueue(new Callback<Entity>() {
            @Override
            public void onResponse(Call<Entity> call, Response<Entity> response) {
                if (response.isSuccessful()) {
                    final Entity entity = response.body();
                    final String id = entity.getId();
                } else {
                    int statusCode = response.code();
                    // handle request errors depending on status code
                }
            }

            @Override
            public void onFailure(Call<Entity> call, Throwable t) {
                Log.e(TAG, "Unable to load entity", t);
            }
        });
    }

    private void loadQueues() {
        AppConfig.getApiService().getQueues(AppConfig.ENTITY_ID).enqueue(new Callback<List<Queue>>() {
            @Override
            public void onResponse(Call<List<Queue>> call, Response<List<Queue>> response) {
                if (response.isSuccessful()) {
                    final List<Queue> queues = response.body();
                    setupQueues(queues);
                } else {
                    int statusCode = response.code();
                    // handle request errors depending on status code
                }
            }

            @Override
            public void onFailure(Call<List<Queue>> call, Throwable t) {
                // TODO - show the error

                Log.e(TAG, "Unable to load queues", t);
            }
        });
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> fragmentTitles = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
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

        public void addFrag(Fragment fragment, String title) {
            fragments.add(fragment);
            fragmentTitles.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }
    }
}
