package Models;

public class Clip {
    private double start;
    private double end;
    private String downloadUrl;
    public Clip(double start, double end) {
        this.start = start;
        this.end = end;
    }

    public double getStart() {
        return start;
    }

    public double getEnd() {
        return end;
    }

    public double getDuration() {
        return end - start;
    }

    public String getFormattedRange() {
        return String.format("%.2f - %.2f sec", start, end);
    }
    public Clip(double start, double end, String downloadUrl) {
        this.start = start;
        this.end = end;
        this.downloadUrl = downloadUrl;
    }
    public String getDownloadUrl() { return downloadUrl; }

}
