package com.clientapp.util.implement;

import com.clientapp.model.MemoryUsage;
import com.clientapp.util.IMemoryUsageCollector;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;

public class MemoryUsageCollector implements IMemoryUsageCollector {
    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hal;
    private final GlobalMemory memory;

    public MemoryUsageCollector(){
        this.systemInfo = new SystemInfo();
        this.hal = systemInfo.getHardware();
        this.memory = systemInfo.getHardware().getMemory();
    }

    @Override
    public MemoryUsage getMemoryUsage() {
        // Lấy thông tin bộ nhớ RAM
        GlobalMemory memory = hal.getMemory();
        long available = memory.getAvailable() / (1024 * 1024);
        long total = memory.getTotal() / (1024 * 1024);
        long inUse = (total - available);

        // Lấy thông tin về tốc độ, slots và form factor
        long speed = 0;
        String ramType = "";
        int slotUsed = 0;
        for (PhysicalMemory physicalMemory : memory.getPhysicalMemory()) {
            speed = physicalMemory.getClockSpeed() /1000000;
            ramType = physicalMemory.getMemoryType();
            slotUsed = memory.getPhysicalMemory().size();
        }

        return new MemoryUsage(total, inUse, available, speed, ramType, slotUsed);
    }
}
