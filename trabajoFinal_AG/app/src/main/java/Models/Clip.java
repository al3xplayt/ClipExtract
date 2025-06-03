package Models;

public class Clip {
    private double end;

    private double start;

    private String fileName;

    public Clip(String fileName, double start ,double end) {
        this.end = end;
        this.start = start;
        this.fileName = fileName;
    }

    public double getEnd() {
        return end;
    }

    public void setEnd(double end) {
        this.end = end;
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
