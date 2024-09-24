package com.clientapp.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemoryUsage {
    long totalMemorySize;
    long usedMemorySize;
    long freeMemory;
    long speed;
    String ramType;
    int slotUsed;

    public MemoryUsage(long totalMemorySize, long usedMemory, long freeMemory, long speed, String ramType, int slotUsed) {
        this.totalMemorySize = totalMemorySize;
        this.usedMemorySize = usedMemory;
        this.freeMemory = freeMemory;
        this.speed = speed;
        this.ramType = ramType;
        this.slotUsed = slotUsed;
    }
}
