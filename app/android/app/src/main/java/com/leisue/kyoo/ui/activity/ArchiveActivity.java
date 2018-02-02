package com.leisue.kyoo.ui.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.leisue.kyoo.R;
import com.leisue.kyoo.databinding.ActivityArchiveBinding;
import com.leisue.kyoo.ui.viewmodel.ArchiveActivityModel;

import butterknife.ButterKnife;

/**
 * Archive activity.
 */

public class ArchiveActivity extends BaseActivity {

    private static final String TAG = "ArchiveActivity";

    ArchiveActivityModel archiveActivityModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set view model
        archiveActivityModel = ViewModelProviders.of(this).get(ArchiveActivityModel.class);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        navigationView.setCheckedItem(R.id.menu_item_archives);
    }

}
