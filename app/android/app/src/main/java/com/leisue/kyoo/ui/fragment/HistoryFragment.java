package com.leisue.kyoo.ui.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.Query;
import com.leisue.kyoo.KyooApp;
import com.leisue.kyoo.R;
import com.leisue.kyoo.commons.FirestoreUtils;
import com.leisue.kyoo.model.Booking;
import com.leisue.kyoo.model.BookingRequest;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.Queue;
import com.leisue.kyoo.ui.adapter.BookingQueueAdapter;
import com.leisue.kyoo.ui.adapter.HistoryQueueAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Historical booking fragment.
 */

public abstract class HistoryFragment extends Fragment implements HistoryQueueAdapter.OnHistorySelectedListener {

    private static final String TAG = "HistoryFragment";

    private static final String QUEUE_BUNDLE_KEY = "queue";

    @BindView(R.id.recycler_bookings)
    RecyclerView bookingsRecycler;

    @BindView(R.id.view_empty)
    ViewGroup emptyView;

    @BindView(R.id.button_add_booking)
    FloatingActionButton addBookingButton;

    @BindView(R.id.button_clear_queue)
    FloatingActionButton clearQueueButton;

    private Query query;
    private BookingQueueAdapter adapter;

    public static BookingQueueFragment newInstance(final Queue queue) {
        BookingQueueFragment queueFragment = new BookingQueueFragment();
        Bundle bundle = new Bundle(1);
        bundle.putSerializable(QUEUE_BUNDLE_KEY, queue);
        queueFragment.setArguments(bundle);
        return queueFragment;
    }

    private Queue queue;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.queue = (Queue) getArguments().getSerializable(QUEUE_BUNDLE_KEY);
        query = FirestoreUtils.getQueryForQueue(this.queue);
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

        /*
        // RecyclerView
        adapter = new BookingQueueAdapter(getActivity(), queue, query, this, bookingsRecycler) {
            @Override
            public void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    bookingsRecycler.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    bookingsRecycler.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.message_load_queue_error , Snackbar.LENGTH_LONG).show();
            }
        };
        */

        bookingsRecycler.setLayoutManager(new LinearLayoutManager(this.getContext()));
        bookingsRecycler.setItemAnimator(new DefaultItemAnimator());
        bookingsRecycler.setAdapter(adapter);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /*
    @Override
    public void onBookingSelected(Booking booking) {
        // A booking is selected
        bookingDialog = new BookingDialogFragment();
        bookingDialog.setBookingListener(this);
        bookingDialog.setBooking(booking);
        bookingDialog.show(getChildFragmentManager(), BookingDialogFragment.TAG);
    }
    */

    @OnClick(R.id.button_add_booking)
    public void onAddBookingClicked(View view) {
        Log.i(TAG, "Add a booking");

    }


    @OnClick(R.id.button_clear_queue)
    public void onClearQueueClicked(View view) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int choice) {
                switch (choice) {
                    case DialogInterface.BUTTON_POSITIVE:
                        clearQueue();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.message_clear_queue)
            .setPositiveButton(R.string.yes, dialogClickListener)
            .setNegativeButton(R.string.no, dialogClickListener).show();

    }

    void clearQueue(){
        final Entity entity = KyooApp.getInstance(getActivity()).getEntity();
        KyooApp.getApiService().clearQueue(entity.getId(), queue.getId()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    // Queue is cleared
                } else {
                    // handle request errors depending on status code
                    int statusCode = response.code();
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getActivity().getString(R.string.message_queue_clear_status_code, statusCode), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "Unable to clear queue", t);
                Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.message_queue_clear_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /*
    @Override
    public void onHistorySelected(Booking booking) {
        Log.i(TAG, "Booking received");

        if (TextUtils.isEmpty(booking.getContactNo()) || booking.getNoOfSeats() <= 0) {
            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.message_invalid_booking_info, Snackbar.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(booking.getId())) {
            createBooking(booking);
        } else {
            updateBooking(booking);
        }
    }
    */


    void createBooking(final Booking booking) {
        final Entity entity = KyooApp.getInstance(getActivity()).getEntity();
        KyooApp.getApiService().createBooking(entity.getId(), queue.getId(), new BookingRequest(booking)).enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.isSuccessful()) {
                    final Booking booking = response.body();
                    Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.message_booking_added, Snackbar.LENGTH_LONG).show();
                } else {
                    // handle request errors depending on status code
                    int statusCode = response.code();
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getActivity().getString(R.string.message_booking_add_status_code, statusCode), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                Log.e(TAG, "Unable to save booking", t);
                Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.message_booking_add_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    void updateBooking(final Booking booking) {
        final Entity entity = KyooApp.getInstance(getActivity()).getEntity();
        KyooApp.getApiService().updateBooking(entity.getId(), queue.getId(), booking.getId(), new BookingRequest(booking)).enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.isSuccessful()) {
                    final Booking booking = response.body();
                    Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.message_booking_added, Snackbar.LENGTH_LONG).show();
                } else {
                    // handle request errors depending on status code
                    int statusCode = response.code();
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getActivity().getString(R.string.message_booking_add_status_code, statusCode), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                Log.e(TAG, "Unable to update booking", t);
                Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.message_booking_add_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
