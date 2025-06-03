package Adapters;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.FileProvider;

import com.example.trabajofinal_ag.R;

import java.io.File;
import java.util.List;

import Api.ApiUtils;
import Models.Clip;

public class ClipAdapter extends RecyclerView.Adapter<ClipAdapter.ClipViewHolder> {

    private final List<Clip> clipList;
    private final Activity activity;

    public ClipAdapter(List<Clip> clips, Activity activity) {
        this.clipList = clips;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ClipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_clip, parent, false);
        return new ClipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClipViewHolder holder, int position) {
        Clip clip = clipList.get(position);
        holder.clipTimes.setText("Clip del " + formatTime(clip.getStart()) + " al " + formatTime(clip.getEnd()));

        // URL para previsualizar el clip
        String videoUrl = ApiUtils.getBaseUrl() + "preview_clip_stream?filename="
                + clip.getFileName() + "&start=" + clip.getStart() + "&end=" + clip.getEnd();

        holder.clipVideoView.setVideoPath(videoUrl);
        holder.clipVideoView.seekTo(1); // Mostrar solo el primer frame

        holder.clipVideoView.setOnErrorListener((mp, what, extra) -> {
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                new android.app.AlertDialog.Builder(activity)
                        .setTitle("Error")
                        .setMessage("No se pudo reproducir el clip de video.")
                        .setPositiveButton("Aceptar", null)
                        .show();
            }
            return true; // Indica que el error fue manejado
        });

        holder.downloadClipBtn.setOnClickListener(v -> {
            ApiUtils.downloadClip(clip, activity);
        });
    }

    @Override
    public int getItemCount() {
        return clipList.size();
    }

    public static class ClipViewHolder extends RecyclerView.ViewHolder {
        VideoView clipVideoView;
        TextView clipTimes;
        Button downloadClipBtn;

        public ClipViewHolder(@NonNull View itemView) {
            super(itemView);
            clipVideoView = itemView.findViewById(R.id.clipVideoView);
            clipTimes = itemView.findViewById(R.id.clipTimes);
            downloadClipBtn = itemView.findViewById(R.id.downloadClipBtn);
        }
    }

    private String formatTime(double seconds) {
        int min = (int) (seconds / 60);
        int sec = (int) (seconds % 60);
        return String.format("%02d:%02d", min, sec);
    }
}
