package Models;

public class ClipHistory {
    private String clipName;
    private String videoName;
    private String timestamp;
    private String duration;

    // Constructor vacío (necesario para crear objetos vacíos antes de asignarles datos)
    public ClipHistory() {}

    // Getters
    public String getClipName() {
        return clipName;
    }

    public String getVideoName() {
        return videoName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getDuration() {
        return duration;
    }

    // Setters
    public void setClipName(String clipName) {
        this.clipName = clipName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
