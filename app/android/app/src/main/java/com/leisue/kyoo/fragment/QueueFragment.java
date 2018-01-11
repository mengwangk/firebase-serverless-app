package com.leisue.kyoo.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.KyooConfig;
import com.leisue.kyoo.R;
import com.leisue.kyoo.activity.MainActivity;
import com.leisue.kyoo.adapter.QueueAdapter;
import com.leisue.kyoo.model.Booking;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.Queue;
import com.leisue.kyoo.util.FirestoreUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class QueueFragment extends Fragment implements
    BookingDialogFragment.BookingListener, QueueAdapter.OnBookingSelectedListener {

    private static final String TAG = "QueueFragment";

    private static final String QUEUE_BUNDLE_KEY = "queue";

    @BindView(R.id.recycler_bookings)
    RecyclerView mBookingsRecycler;

    @BindView(R.id.view_empty)
    ViewGroup mEmptyView;

    @BindView(R.id.button_add_booking)
    FloatingActionButton mAddBookingButton;

    private Query mQuery;
    private QueueAdapter mAdapter;
    private BookingDialogFragment mBookingDialog;

    public static QueueFragment newInstance(final Queue queue) {
        QueueFragment queueFragment = new QueueFragment();
        Bundle bundle = new Bundle(1);
        bundle.putSerializable(QUEUE_BUNDLE_KEY, queue);
        queueFragment.setArguments(bundle);
        return queueFragment;
    }

    private Queue queue;

    public QueueFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.queue = (Queue) getArguments().getSerializable(QUEUE_BUNDLE_KEY);
        mQuery = FirestoreUtil.getBookings(this.queue);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_queue, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        // RecyclerView
        mAdapter = new QueueAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mBookingsRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mBookingsRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(getActivity().findViewById(android.R.id.content), "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        mBookingsRecycler.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mBookingsRecycler.setAdapter(mAdapter);

        // Booking Dialog
        mBookingDialog = new BookingDialogFragment();
        mBookingDialog.setBookingListener(this);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void onBookingSelected(DocumentSnapshot restaurant) {
        // A booking is selected
    }

    @OnClick(R.id.button_add_booking)
    public void onAddBookingClicked(View view) {
        Log.i(TAG, "Add a booking");
        mBookingDialog.show(getChildFragmentManager(), BookingDialogFragment.TAG);
    }

    @Override
    public void onBooking(Booking booking) {
        Log.i(TAG, "Booking received");

        if (TextUtils.isEmpty(booking.getContactNo()) || booking.getNoOfCustomers() <= 0)
            Snackbar.make(getActivity().findViewById(android.R.id.content), "Invalid booking info.", Snackbar.LENGTH_LONG).show();

        saveBooking(booking);
    }

    void saveBooking(final Booking booking) {
        final Entity entity = KyooApp.getInstance(getActivity()).getEntity();
        KyooApp.getApiService().saveBooking(entity.getId(), queue.getId(), booking).enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.isSuccessful()) {
                    final Booking booking = response.body();
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Booking added successfully.", Snackbar.LENGTH_LONG).show();
                } else {
                    // handle request errors depending on status code
                    int statusCode = response.code();
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Unable to add booking. Status code is " + statusCode, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                Log.e(TAG, "Unable to load entity", t);
                Snackbar.make(getActivity().findViewById(android.R.id.content), "Unable to add booking. Contact the support.", Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
