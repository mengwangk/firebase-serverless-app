package com.leisue.kyoo.ui.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.R;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.EntityRequest;
import com.leisue.kyoo.ui.viewmodel.BaseActivityViewModel;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Base class for all activities.
 */

public abstract class BaseActivity extends AppCompatActivity {

    public interface OnEntityLoadedListener {
        void onEntityLoaded(final Entity entity);
    }

    OnEntityLoadedListener entityListener;

    private static final String TAG = "BaseActivity";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    @BindView(R.id.drawer)
    DrawerLayout drawerLayout;

    BaseActivityViewModel viewModel;

    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View model
        viewModel = ViewModelProviders.of(this).get(BaseActivityViewModel.class);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn();
        }
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            final IdpResponse response = IdpResponse.fromResultIntent(data);

            viewModel.setIsSigningIn(false);

            if (resultCode != RESULT_OK && shouldStartSignIn()) {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Log.e(TAG,"Login canceled by User");
                    return;
                }
                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Log.e(TAG,"No Internet Connection");
                    return;
                }
                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Log.e(TAG,"Unknown Error");
                    return;
                }
                startSignIn();
            } else {
                onUserSignedIn();
            }
        }
    }


    void launchActivity(Class clazz) {
        if (this.getClass() != clazz) {
            Intent intent = new Intent(BaseActivity.this, clazz);
            startActivity(intent);
            finish();
        }
    }

    void setHeaderView() {
        final Entity entity = KyooApp.getInstance(this).getEntity();
        if (entity != null) {
            View headerLayout = navigationView.getHeaderView(0);
            TextView name = headerLayout.findViewById(R.id.tv_name);
            TextView email = headerLayout.findViewById(R.id.tv_email);
            name.setText(entity.getName());
            email.setText(entity.getEmail());
        }
    }

    void setEntityListener(OnEntityLoadedListener listener) {
        this.entityListener = listener;
    }

    protected boolean shouldStartSignIn() {
        return (!viewModel.getIsSigningIn() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    protected boolean isSignedIn() {
        return (FirebaseAuth.getInstance().getCurrentUser() != null);
    }

    protected void startSignIn() {
        // Sign in with FirebaseUI
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(Collections.singletonList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
            .setIsSmartLockEnabled(true)
            .setTheme(R.style.LoginTheme)
            .setLogo(R.mipmap.logo)
            .build();

        startActivityForResult(intent, RC_SIGN_IN);
        viewModel.setIsSigningIn(true);
    }

    protected void onUserSignedIn() {
        // Send a verification email
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null && !user.isEmailVerified()) {
            user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Snackbar.make(findViewById(android.R.id.content), R.string.message_email_verification, Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
        }
    }

    protected void loadEntity() {
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            KyooApp.getApiService().getEntityByEmail(user.getEmail()).enqueue(new Callback<List<Entity>>() {
                @Override
                public void onResponse(Call<List<Entity>> call, Response<List<Entity>> response) {
                    if (response.isSuccessful()) {
                        final List<Entity> entities = response.body();
                        if (entityListener != null)
                            entityListener.onEntityLoaded(entities.get(0));
                    } else {
                        // handle request errors depending on status code
                        int statusCode = response.code();
                        if (statusCode == 404) {    // The entity is not found, proceed to create a new one
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.message_entity_create, user.getEmail()), Snackbar.LENGTH_LONG).show();
                            createEntity();
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.message_entity_load_status_code, statusCode), Snackbar.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Entity>> call, Throwable t) {
                    Log.e(TAG, "Unable to load entity", t);
                    Snackbar.make(findViewById(android.R.id.content), R.string.message_entity_load_error, Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    protected void createEntity(){
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        final Entity entity = new Entity();
        entity.setEmail(user.getEmail());
        entity.setName(user.getDisplayName());

        KyooApp.getApiService().createEntity(new EntityRequest(entity)).enqueue(new Callback<Entity>() {
            @Override
            public void onResponse(Call<Entity> call, Response<Entity> response) {
                if (response.isSuccessful()) {
                    final Entity entity = response.body();
                    Snackbar.make(findViewById(android.R.id.content), R.string.message_entity_configured, Snackbar.LENGTH_LONG).show();
                    loadEntity();
                } else {
                    // handle request errors depending on status code
                    int statusCode = response.code();
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.message_entity_configure_status_code, statusCode), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Entity> call, Throwable t) {
                Log.e(TAG, "Unable to create entity", t);
                Snackbar.make(findViewById(android.R.id.content), R.string.message_entity_configure_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
