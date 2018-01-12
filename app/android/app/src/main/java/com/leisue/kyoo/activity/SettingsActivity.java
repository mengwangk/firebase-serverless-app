package com.leisue.kyoo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;

import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.R;
import com.leisue.kyoo.model.Entity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Settings activity.
 */

public class SettingsActivity extends BaseActivity {

    @BindView(R.id.edit_entity_name)
    EditText mEntityName;

    @BindView(R.id.edit_entity_email)
    EditText mEntityEmail;

    @BindView(R.id.edit_entity_id)
    EditText mEntityId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        displayEntity();
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        navigationView.setCheckedItem(R.id.menu_item_settings);
    }

    void displayEntity(){
        final Entity entity = KyooApp.getInstance(this).getEntity();
        if (entity != null) {
            mEntityName.setText(entity.getName());
            mEntityEmail.setText(entity.getEmail());
            mEntityId.setText(entity.getId());
        }
    }
}
