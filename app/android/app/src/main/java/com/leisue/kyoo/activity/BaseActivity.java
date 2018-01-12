package com.leisue.kyoo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.R;
import com.leisue.kyoo.model.Entity;

import butterknife.BindView;

/**
 * Base class for all activities.
 */

public abstract class BaseActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    @BindView(R.id.drawer)
    DrawerLayout drawerLayout;

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.isChecked())
                    menuItem.setChecked(false);
                else
                    menuItem.setChecked(true);

                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.menu_item_queue:
                        launchActivity(MainActivity.class);
                        break;
                    case R.id.menu_item_settings:
                        launchActivity(SettingsActivity.class);
                        break;
                }
                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        // Setting the actionbarToggle to drawer layout
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        // Calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        setHeaderView();
    }

    void launchActivity(Class clazz) {
        if (this.getClass() != clazz) {
            startActivity(new Intent(BaseActivity.this, clazz));
        }
    }

    void setHeaderView(){
        final Entity entity = KyooApp.getInstance(this).getEntity();
        if (entity != null) {
            View headerLayout = navigationView.getHeaderView(0);
            TextView name = headerLayout.findViewById(R.id.tv_name);
            TextView email = headerLayout.findViewById(R.id.tv_email);
            name.setText(entity.getName());
            email.setText(entity.getEmail());
        }
    }
}
