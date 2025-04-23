package Database;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.trabajofinal_ag.MainActivity;
import com.example.trabajofinal_ag.R;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<DownloadHistory> downloadHistoryList;
    private Context context;

    public HistoryAdapter(List<DownloadHistory> downloadHistoryList, Context context) {
        this.downloadHistoryList = downloadHistoryList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DownloadHistory history = downloadHistoryList.get(position);
        holder.fileName.setText(history.getFileName());
        holder.fileDate.setText(history.getDate());
        holder.fileFormat.setText(history.getFormat());

        // Configurar el botón de descarga
        holder.downloadButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("url", history.getUrl());
            bundle.putString("format", history.getFormat());
            intent.putExtras(bundle);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return downloadHistoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView fileName;
        TextView fileDate;
        TextView fileFormat;
        ImageButton downloadButton;

        public ViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            fileDate = itemView.findViewById(R.id.fileDate);
            fileFormat = itemView.findViewById(R.id.fileFormat);
            downloadButton = itemView.findViewById(R.id.downloadButton);
        }
    }
}
