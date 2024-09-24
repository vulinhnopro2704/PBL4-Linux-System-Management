package com.serverapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
public class MemoryUsage {
    public long totalMemorySize;
    public long usedMemorySize;
    public long freeMemory;
    public long speed;
    public String ramType;
    public int slotUsed;

    // Constructor rỗng
    public MemoryUsage() {
    }

    @Override
    public String toString() {
        return "MemoryUsage{" +
                "totalMemorySize=" + totalMemorySize +
                ", usedMemorySize=" + usedMemorySize +
                ", freeMemory=" + freeMemory +
                '}';
    }
}
