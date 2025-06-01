package Adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajofinal_ag.R;

import java.util.List;

import Models.Clip;
import Api.ApiUtils;
import Api.ApiCallback;

import org.json.JSONObject;

public class ClipAdapter extends RecyclerView.Adapter<ClipAdapter.ClipViewHolder> {

    private List<Clip> clipList;
    private Context context;

    public ClipAdapter(Context context, List<Clip> clips) {
        this.context = context;
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
        holder.clipVideoView.setOnPreparedListener(mp -> {
            mp.seekTo((int) (clip.getStart() * 1000)); // posición de inicio en ms
            mp.setVolume(0f, 0f); // sin sonido para preview, opcional
            mp.start();

            // Detener después del clip
            new Handler().postDelayed(() -> {
                if (holder.clipVideoView.isPlaying()) {
                    holder.clipVideoView.pause();
                }
            }, (long) ((clip.getEnd() - clip.getStart()) * 1000));
        });

        // Botón de descarga
        holder.downloadBtn.setOnClickListener(v -> {
            // Llamar al backend para descargar el clip
            ApiUtils.downloadClip(clip, new ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    // Aquí puedes mostrar mensaje, actualizar UI, etc.
                    // La descarga debe guardarse localmente en el dispositivo
                }

                @Override
                public void onFailure(Exception e) {
                    // Mostrar error
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return clipList.size();
    }

    static class ClipViewHolder extends RecyclerView.ViewHolder {
        TextView clipRange;
        VideoView clipVideoView;
        Button downloadBtn;

        public ClipViewHolder(@NonNull View itemView) {
            super(itemView);
            clipRange = itemView.findViewById(R.id.clipTimes);
            clipVideoView = itemView.findViewById(R.id.clipVideoView);
            downloadBtn = itemView.findViewById(R.id.downloadClipBtn);
        }
    }
}
