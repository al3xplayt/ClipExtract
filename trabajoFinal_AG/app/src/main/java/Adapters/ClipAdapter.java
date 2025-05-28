package Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajofinal_ag.R;

import java.util.List;

import Models.Clip;

public class ClipAdapter extends RecyclerView.Adapter<ClipAdapter.ClipViewHolder> {

    private List<Clip> clipList;

    public ClipAdapter(List<Clip> clips) {
        this.clipList = clips;
    }

    @NonNull
    @Override
    public ClipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clip, parent, false);
        return new ClipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClipViewHolder holder, int position) {
        Clip clip = clipList.get(position);
        holder.clipRange.setText(clip.getFormattedRange());
        holder.clipDuration.setText("Duración: " + String.format("%.2f", clip.getDuration()) + " sec");
    }

    @Override
    public int getItemCount() {
        return clipList.size();
    }

    static class ClipViewHolder extends RecyclerView.ViewHolder {
        TextView clipRange, clipDuration;

        public ClipViewHolder(@NonNull View itemView) {
            super(itemView);
            clipRange = itemView.findViewById(R.id.clipTimes);
            
        }
    }
}