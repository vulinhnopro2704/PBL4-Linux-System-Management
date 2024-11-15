package com.serverapp.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class FileSendDetail {

    private final SimpleStringProperty fileName;
    private final SimpleDoubleProperty progress;  // Tiến độ gửi file (từ 0.0 đến 1.0)
    private final SimpleStringProperty ipAddress;
    private final SimpleLongProperty sizeFile;

    public FileSendDetail(String fileName, double progress, String ipAddress, long sizeFile) {
        this.fileName = new SimpleStringProperty(fileName);
        this.progress = new SimpleDoubleProperty(progress);
        this.ipAddress = new SimpleStringProperty(ipAddress);
        this.sizeFile = new SimpleLongProperty(sizeFile);
    }

    public String getFileName() {
        return fileName.get();
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public double getProgress() {
        return progress.get();
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    public DoubleProperty progressProperty() { // Thêm phương thức này
        return progress;
    }

    public long getSizeFile() {
        return sizeFile.get();
    }

    public void setSizeFile(long sizeFile) {
        this.sizeFile.set(sizeFile);
    }

    public String getIpAddress() {
        return ipAddress.get();
    }

    public SimpleStringProperty ipAddressProperty() {
        return ipAddress;
    }
}
