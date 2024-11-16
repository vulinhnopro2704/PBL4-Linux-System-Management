package com.serverapp.model;

import com.google.gson.Gson;

public class ScreenPacket {
    public int totalChunks;
    public int chunkIndex;
    public int length;
    public byte[] data;

    public ScreenPacket(int totalChunks, int chunkIndex, int length, byte[] data) {
        this.totalChunks = totalChunks;
        this.chunkIndex = chunkIndex;
        this.length = length;
        this.data = data;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static ScreenPacket fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, ScreenPacket.class);
    }
}