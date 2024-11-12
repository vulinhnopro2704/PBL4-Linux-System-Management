package com.clientapp.model;

import java.util.List;

public class ClamAVResponse {
    public String message;
    List<String> infectedFiles;

    public ClamAVResponse(String message, List<String> infectedFiles) {
        this.message = message;
        this.infectedFiles = infectedFiles;
    }
}
