package main.sos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {
    
    private List<MainActivity.Report> reports;
    private OnViewMapClickListener onViewMapClickListener;
    
    public interface OnViewMapClickListener {
        void onViewMapClick(MainActivity.Report report);
    }
    
    public ReportsAdapter(List<MainActivity.Report> reports, OnViewMapClickListener onViewMapClickListener) {
        this.reports = reports;
        this.onViewMapClickListener = onViewMapClickListener;
    }
    
    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        MainActivity.Report report = reports.get(position);
        
        holder.tvReporterName.setText(report.name);
        holder.tvTimestamp.setText(report.timestamp);
        holder.tvStatus.setText(report.status);
        
        holder.tvLocation.setText(holder.itemView.getContext().getString(
            R.string.coordinates,
            String.valueOf(report.location.lat),
            String.valueOf(report.location.lng)
        ));
        
        holder.tvContact.setText(holder.itemView.getContext().getString(
            R.string.contact_label,
            report.contact
        ));
        
        holder.tvDetails.setText(report.details);
        
        // Show/hide relayed badge
        holder.layoutRelayedBadge.setVisibility(report.relayed ? View.VISIBLE : View.GONE);
        
        // Set click listener for map button
        holder.btnViewOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewMapClickListener.onViewMapClick(report);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return reports.size();
    }
    
    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        public TextView tvReporterName;
        public TextView tvTimestamp;
        public TextView tvStatus;
        public TextView tvLocation;
        public TextView tvContact;
        public TextView tvDetails;
        public LinearLayout layoutRelayedBadge;
        public MaterialButton btnViewOnMap;
        
        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReporterName = itemView.findViewById(R.id.tvReporterName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            layoutRelayedBadge = itemView.findViewById(R.id.layoutRelayedBadge);
            btnViewOnMap = itemView.findViewById(R.id.btnViewOnMap);
        }
    }
}