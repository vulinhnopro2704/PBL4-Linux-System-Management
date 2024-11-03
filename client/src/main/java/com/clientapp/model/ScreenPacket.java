package com.clientapp.model;

import lombok.Getter;

import java.io.*;

@Getter
public class ScreenPacket {
    @Getter
    private int totalChunks;
    private int chunkIndex;
    private int length;
    @Getter
    private byte[] data;

    public ScreenPacket(int totalChunks, int chunkIndex, int length, byte[] data) {
        this.totalChunks = totalChunks;
        this.chunkIndex = chunkIndex;
        this.length = length;
        this.data = data;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(totalChunks);
        dos.writeInt(chunkIndex);
        dos.writeInt(length);
        dos.write(data);
        return baos.toByteArray();
    }

    public static ScreenPacket fromByteArray(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);
        int totalChunks = dis.readInt();
        int chunkIndex = dis.readInt();
        int length = dis.readInt();
        byte[] data = new byte[length];
        dis.readFully(data);
        return new ScreenPacket(totalChunks, chunkIndex, length, data);
    }

}

