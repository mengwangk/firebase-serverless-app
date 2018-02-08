package com.leisue.kyoo.ui.adapter;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.leisue.kyoo.R;
import com.leisue.kyoo.model.ArchiveSummary;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

/**
 * Archive summary adapter.
 */
public class ArchiveSummaryAdapter extends FirestoreRecyclerAdapter<ArchiveSummary, ArchiveSummaryAdapter.ArchiveSummaryViewHolder> {

    private static final String TAG = "ArchiveSummaryAdapter";

    public interface OnArchiveSummarySelectedListener {
        void onArchiveSummarySelected(ArchiveSummary archiveSummary);
    }

    private ArchiveSummaryAdapter.OnArchiveSummarySelectedListener listener;

    private RecyclerView recyclerView;

    protected ArchiveSummaryAdapter(FragmentActivity activity, Query query, RecyclerView recyclerView, ArchiveSummaryAdapter.OnArchiveSummarySelectedListener listener) {
        super(new FirestoreRecyclerOptions.Builder<ArchiveSummary>()
            .setQuery(query, ArchiveSummary.class)
            .setLifecycleOwner(activity)
            .build());

        this.recyclerView = recyclerView;
        this.listener = listener;
    }

    @Override
    public ArchiveSummaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ArchiveSummaryViewHolder(inflater.inflate(R.layout.item_archive_summary, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull ArchiveSummaryViewHolder holder, int position, @NonNull ArchiveSummary model) {
        holder.bind(model, listener);
    }

    public void filter(Date from, Date to) {
        Date nextDayTo = addDays(to, 1);
        for (int i = 0; i < getItemCount(); i++) {
            ArchiveSummary archiveSummary = getItem(i);
            // Put the correct filtering logic here
            if (archiveSummary.getFromDate() >= from.getTime() && archiveSummary.getToDate() <= nextDayTo.getTime()) {
                archiveSummary.setVisible(true);
            } else {
                archiveSummary.setVisible(false);
            }
        }
        notifyDataSetChanged();
    }

    Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

    public class ArchiveSummaryViewHolder extends RecyclerView.ViewHolder {

        ArchiveSummary archiveSummary;

        @BindView(R.id.archive_date)
        TextView archiveDate;

        @BindView(R.id.archive_time)
        TextView archiveTime;

        @BindView(R.id.archive_total_bookings)
        TextView archiveTotalBookings;

        @BindView(R.id.checkbox_select_archive)
        CheckBox selectArchive;

        @BindView(R.id.layout_archive_summary)
        RelativeLayout layout;

        ArchiveSummaryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(final ArchiveSummary archiveSummary, final ArchiveSummaryAdapter.OnArchiveSummarySelectedListener listener) {

            final SimpleDateFormat sfDate = new SimpleDateFormat("yyyy-MM-dd");
            final SimpleDateFormat sfTime = new SimpleDateFormat("hh:mm a");
            //sfDate.setTimeZone(TimeZone.getDefault());
            //sfTime.setTimeZone(TimeZone.getDefault());

            final Resources resources = itemView.getResources();
            this.archiveSummary = archiveSummary;
            Date fromDate = new Date(this.archiveSummary.getFromDate());
            Date toDate = new Date(this.archiveSummary.getToDate());
            String fromDateFormatted = sfDate.format(fromDate);
            String toDateFormatted = sfDate.format(toDate);
            if (fromDateFormatted.equals(toDateFormatted)) {
                archiveDate.setText(fromDateFormatted);
            } else {
                archiveDate.setText(fromDateFormatted + "-" + toDateFormatted);
            }

            String fromTimeFormatted = sfTime.format(fromDate);
            String toTimeFormatted = sfTime.format(toDate);
            archiveTime.setText(fromTimeFormatted + "-" + toTimeFormatted);

            archiveTotalBookings.setText(this.archiveSummary.getTotalBookings().toString());

            if (archiveSummary.isVisible()) {
                show();
            } else {
                hide();
            }

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onArchiveSummarySelected(archiveSummary);
                    }
                }
            });
        }

        void hide() {
            ViewGroup.LayoutParams params = layout.getLayoutParams();
            params.height = 0;
            layout.setLayoutParams(params);
        }

        void show() {
            ViewGroup.LayoutParams params = layout.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layout.setLayoutParams(params);
        }

        @OnCheckedChanged(R.id.checkbox_select_archive)
        public void onCheckChanged(CompoundButton button, boolean isChecked) {
            Log.i(TAG, "ischecked " + isChecked);
            this.archiveSummary.setSelected(isChecked);
        }

    }
}
