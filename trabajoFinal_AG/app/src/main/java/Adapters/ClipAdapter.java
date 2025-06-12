package Adapters;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajofinal_ag.R;


import java.util.ArrayList;
import java.util.List;

import Api.ApiUtils;
import Models.Clip;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;


public class ClipAdapter extends RecyclerView.Adapter<ClipAdapter.ClipViewHolder> {

    private final List<Clip> clipList;
    private final Activity activity;

    // Mantén una referencia para liberar recursos
    private final List<ExoPlayer> playerList = new ArrayList<>();

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

        String videoUrl = ApiUtils.getBaseUrl() + "preview_clip_stream?filename="
                + clip.getFileName() + "&start=" + clip.getStart() + "&end=" + clip.getEnd();

        // Crear ExoPlayer
        ExoPlayer player = new ExoPlayer.Builder(activity).build();

        // Preparar MediaItem
        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        player.setMediaItem(mediaItem);

        // Asignar player al PlayerView
        holder.clipPlayerView.setPlayer(player);

        // Preparar y pausar para mostrar primer frame
        player.prepare();
        player.seekTo(1);
        player.pause();

        // Guardar referencia para liberar después
        playerList.add(player);
        SharedPreferences sharedPreferences = activity.getSharedPreferences("user_data", Activity.MODE_PRIVATE);
        String username = sharedPreferences.getString("user_name", null);
        holder.downloadClipBtn.setOnClickListener(v -> {
            ApiUtils.downloadClip(clip, activity, username);
        });
    }

    @Override
    public int getItemCount() {
        return clipList.size();
    }

    public static class ClipViewHolder extends RecyclerView.ViewHolder {
        PlayerView clipPlayerView;
        TextView clipTimes;
        Button downloadClipBtn;

        public ClipViewHolder(@NonNull View itemView) {
            super(itemView);
            clipPlayerView = itemView.findViewById(R.id.clipPlayerView);
            clipTimes = itemView.findViewById(R.id.clipTimes);
            downloadClipBtn = itemView.findViewById(R.id.downloadClipBtn);
        }
    }

    private String formatTime(double seconds) {
        int min = (int) (seconds / 60);
        int sec = (int) (seconds % 60);
        return String.format("%02d:%02d", min, sec);
    }

    // Añade método para liberar recursos cuando el RecyclerView se destruya o la activity termine
    public void releasePlayers() {
        for (ExoPlayer player : playerList) {
            player.release();
        }
        playerList.clear();
    }
}

