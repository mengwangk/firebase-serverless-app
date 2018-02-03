package com.leisue.kyoo.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.leisue.kyoo.GlideApp;
import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.R;
import com.leisue.kyoo.model.CreateUserRequest;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.EntityRequest;
import com.leisue.kyoo.model.Lookup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Settings activity.
 */

public class SettingsActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

    private static final int RC_CHOOSE_PHOTO = 101;
    private static final int RC_IMAGE_PERMS = 102;
    private static final String PERMS = Manifest.permission.READ_EXTERNAL_STORAGE;

    private static final String TAG = "SettingsActivity";

    @BindView(R.id.edit_entity_name)
    EditText entityName;

    @BindView(R.id.edit_entity_email)
    EditText entityEmail;

    @BindView(R.id.edit_entity_id)
    EditText entityId;

    @BindView(R.id.spinner_entity_industry)
    Spinner entityIndustry;

    @BindView(R.id.button_configure_queue)
    Button configureQueueButton;

    @BindView(R.id.button_apply)
    Button applyButton;

    @BindView(R.id.button_upload)
    Button uploadButton;

    @BindView(R.id.img_avatar)
    ImageView avatarImage;

    // Firebase cloud storage image reference
    private FirebaseStorage firebaseStorage;

    private static final int THUMBNAIL_SIZE = 200;
    private static final String AVATAR_FILE_NAME = "avatar.png";

    Uri selectedImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initStorage();
        showEntityDetails();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        navigationView.setCheckedItem(R.id.menu_item_settings);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) {
                selectedImage = data.getData();
                uploadPhoto(selectedImage);
            } else {
                Snackbar.make(findViewById(android.R.id.content), R.string.message_no_photo_chosen, Snackbar.LENGTH_LONG).show();
            }
        } else if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE && EasyPermissions.hasPermissions(this, PERMS)) {
            choosePhoto();
        }
    }

    void showEntityDetails() {
        final Entity entity = KyooApp.getInstance(this).getEntity();
        if (entity != null) {
            entityName.setText(entity.getName());
            entityEmail.setText(entity.getEmail());
            entityId.setText(entity.getId());
            loadIndustry(entity);
            loadPhoto(getStorageReference(entity));
        }
        entityId.setEnabled(false);
        entityEmail.setEnabled(false);
    }

    @OnClick(R.id.button_apply)
    public void onEntityUpdated() {
        Log.i(TAG, "Update the entity");

        // Update the entity details
        final Entity entity = KyooApp.getInstance(this).getEntity();
        entity.setName(entityName.getText().toString());
        entity.setIndustry(entityIndustry.getSelectedItem().toString());
        KyooApp.getApiService().updateEntity(entity.getId(), new EntityRequest(entity)).enqueue(new Callback<Entity>() {
            @Override
            public void onResponse(Call<Entity> call, Response<Entity> response) {
                if (response.isSuccessful()) {
                    final Entity entity = response.body();
                    KyooApp.getInstance(SettingsActivity.this).setEntity(entity);
                    setHeaderView();
                    Snackbar.make(findViewById(android.R.id.content), R.string.message_entity_configured, Snackbar.LENGTH_LONG).show();
                } else {
                    // handle request errors depending on status code
                    int statusCode = response.code();
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.message_entity_configure_status_code, statusCode), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Entity> call, Throwable t) {
                Log.e(TAG, "Unable to update entity", t);
                Snackbar.make(findViewById(android.R.id.content), R.string.message_entity_configure_error, Snackbar.LENGTH_LONG).show();
            }
        });

    }

    @OnClick(R.id.button_upload)
    @AfterPermissionGranted(RC_IMAGE_PERMS)
    protected void choosePhoto() {
        if (!EasyPermissions.hasPermissions(this, PERMS)) {
            EasyPermissions.requestPermissions(this, getString(R.string.rational_upload_avatar), RC_IMAGE_PERMS, PERMS);
            return;
        }

        Log.i(TAG, "Upload an avatar.");
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(i, RC_CHOOSE_PHOTO);
    }

    @OnClick(R.id.button_configure_queue)
    public void onConfigureQueue() {
        launchActivity(QueueConfigActivity.class);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // See @AfterPermissionGranted
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, Collections.singletonList(PERMS))) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    void initStorage() {
        firebaseStorage = FirebaseStorage.getInstance();
    }

    StorageReference createStorageReference() {
        final Entity entity = KyooApp.getInstance(this).getEntity();
        final String appName = KyooApp.getAppName();
        // final String fileName = UUID.randomUUID().toString();
        final String fileReference = appName + "/" + entity.getId() + "/" + AVATAR_FILE_NAME;
        return firebaseStorage.getReference(fileReference);
    }

    StorageReference getStorageReference(final Entity entity) {
        if (!TextUtils.isEmpty(entity.getAvatar())) {
            return firebaseStorage.getReference(entity.getAvatar());
        }
        return null;
    }

    void uploadPhoto(Uri uri) {
        Snackbar.make(findViewById(android.R.id.content), R.string.message_uploading, Snackbar.LENGTH_LONG).show();

        try {
            final StorageReference storageReference = createStorageReference();
            final byte[] imageData = resizeBitmap(uri);
            storageReference.putBytes(imageData).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Entity entity = KyooApp.getInstance(SettingsActivity.this).getEntity();
                    entity.setAvatar(taskSnapshot.getMetadata().getReference().getPath());
                    loadPhoto(storageReference);
                    Snackbar.make(findViewById(android.R.id.content), R.string.message_upload_success, Snackbar.LENGTH_LONG).show();
                }
            })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "uploadPhoto:onError", e);
                            Snackbar.make(findViewById(android.R.id.content), R.string.message_upload_failed, Snackbar.LENGTH_LONG).show();
                        }
                    });
        } catch (IOException e) {
            Snackbar.make(findViewById(android.R.id.content), R.string.message_upload_failed, Snackbar.LENGTH_LONG).show();
        }
    }

    void loadIndustry(final Entity entity) {
        KyooApp.getApiService().getLookup(Lookup.TYPE.INDUSTRY.getId()).enqueue(new Callback<Lookup>() {
            @Override
            public void onResponse(Call<Lookup> call, Response<Lookup> response) {
                if (response.isSuccessful()) {
                    final Lookup industry = response.body();
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(SettingsActivity.this, android.R.layout.simple_spinner_item, industry.getValues());
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    entityIndustry.setAdapter(adapter);
                    if (!TextUtils.isEmpty(entity.getIndustry())) {
                        int pos = adapter.getPosition(entity.getIndustry());
                        entityIndustry.setSelection(pos);
                    }
                } else {
                    // handle request errors depending on status code
                    int statusCode = response.code();
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.message_industry_load_status_code, statusCode), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Lookup> call, Throwable t) {
                Log.e(TAG, "Unable to load industry", t);
                Snackbar.make(findViewById(android.R.id.content), R.string.message_industry_load_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    void loadPhoto(final StorageReference storageReference) {
        if (storageReference != null) {
            GlideApp.with(SettingsActivity.this)
                    .load(storageReference)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .override(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(avatarImage);
        }
    }

    byte[] resizeBitmap(final Uri uri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        bitmap = Bitmap.createScaledBitmap(bitmap, THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }


    @OnClick(R.id.button_create_user)
    public void onCreateNewUser() {
        Log.i(TAG, "Create a new user");

        // Update the entity details
        final Entity entity = new Entity();
        entity.setEmail("upload_user123@gmail.com");
        entity.setName("test user");
        entity.setIndustry(entityIndustry.getSelectedItem().toString());

        // Proceed to set other values as well

        final CreateUserRequest createUserRequest = new CreateUserRequest(entity, "password123");       // Set the password
        try {
            MultipartBody.Part photo = null;
            if (this.selectedImage != null) {
                byte[] imageBytes = resizeBitmap(this.selectedImage);
                RequestBody reqFile = RequestBody.create(MediaType.parse("image/png"), imageBytes);
                photo = MultipartBody.Part.createFormData("avatar", AVATAR_FILE_NAME, reqFile); // DO not change the name
            }
            RequestBody request = RequestBody.create(okhttp3.MultipartBody.FORM, new Gson().toJson(createUserRequest));
            KyooApp.getApiService().createUserAndEntity(request, photo).enqueue(new Callback<Entity>() {
                @Override
                public void onResponse(Call<Entity> call, Response<Entity> response) {
                    if (response.isSuccessful()) {
                        final Entity entity = response.body();
                        KyooApp.getInstance(SettingsActivity.this).setEntity(entity);
                        setHeaderView();
                        Snackbar.make(findViewById(android.R.id.content), R.string.message_entity_configured, Snackbar.LENGTH_LONG).show();
                    } else {
                        // handle request errors depending on status code
                        Log.e(TAG, "Raw : " + response.raw().toString());
                        Log.e(TAG, "Error body: " + response.errorBody().toString());
                        int statusCode = response.code();
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.message_entity_configure_status_code, statusCode), Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Entity> call, Throwable t) {
                    Log.e(TAG, "Unable to update entity", t);
                    Snackbar.make(findViewById(android.R.id.content), R.string.message_entity_configure_error, Snackbar.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Unable to create user", e);
            Snackbar.make(findViewById(android.R.id.content), R.string.message_entity_configure_error, Snackbar.LENGTH_LONG).show();
        }

    }

}
