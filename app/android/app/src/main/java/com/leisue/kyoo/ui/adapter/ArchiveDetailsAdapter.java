package com.leisue.kyoo.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leisue.kyoo.R;
import com.leisue.kyoo.model.ArchiveDetails;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Archive details
 */

public class ArchiveDetailsAdapter extends RecyclerView.Adapter<ArchiveDetailsAdapter.ArchiveDetailsViewHolder> {

    private static final String TAG = "ArchiveDetailsAdapter";

    private List<ArchiveDetails> details;

    private RecyclerView recyclerView;

    public class ArchiveDetailsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.booking_no)
        TextView bookingNo;


        public ArchiveDetailsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, this.itemView);
        }

        void bind(final ArchiveDetails archiveDetails) {
            bookingNo.setText(archiveDetails.getBookingNo());
        }
    }

    public ArchiveDetailsAdapter(List<ArchiveDetails> details, RecyclerView recyclerView) {
        this.details = details;
        this.recyclerView = recyclerView;
    }

    @Override
    public ArchiveDetailsAdapter.ArchiveDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_archive_details, parent, false);
        return new ArchiveDetailsAdapter.ArchiveDetailsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ArchiveDetailsAdapter.ArchiveDetailsViewHolder holder, int position) {
        ArchiveDetails archiveDetails = details.get(position);
        holder.bind(archiveDetails);
    }

    @Override
    public int getItemCount() {
        return details.size();
    }
}
