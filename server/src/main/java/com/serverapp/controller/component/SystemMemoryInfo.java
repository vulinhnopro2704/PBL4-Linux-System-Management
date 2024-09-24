package com.serverapp.controller.component;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.CentralProcessor;
import oshi.software.os.OperatingSystem;

public class SystemMemoryInfo {
    public static void main(String[] args) {
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        OperatingSystem os = systemInfo.getOperatingSystem();

        // Lấy thông tin bộ nhớ RAM
        GlobalMemory memory = hal.getMemory();
        long available = memory.getAvailable();
        long total = memory.getTotal();
        long inUse = total - available;
        long committedVirtualMemory = memory.getVirtualMemory().getSwapUsed() / (1024 * 1024);
        long cachedMemory = memory.getVirtualMemory().getSwapUsed(); // Có thể khác chút so với ảnh
        long pagedPool = memory.getVirtualMemory().getSwapTotal();
        long nonPagedPool = memory.getVirtualMemory().getSwapUsed() - pagedPool; // Tương tự

        System.out.println("In use (Compressed): " + inUse / (1024 * 1024) + " MB");
        System.out.println("Available: " + available / (1024 * 1024) + " MB");
        System.out.println("Committed: " + committedVirtualMemory / (1024 * 1024) + " MB");
        System.out.println("Cached: " + cachedMemory / (1024 * 1024) + " MB");
        System.out.println("Paged pool: " + pagedPool / (1024 * 1024) + " MB");
        System.out.println("Non-paged pool: " + nonPagedPool / (1024 * 1024) + " MB");

        // Lấy thông tin về tốc độ, slots và form factor
        for (PhysicalMemory physicalMemory : memory.getPhysicalMemory()) {
            System.out.println("Speed: " + physicalMemory.getClockSpeed() /1000000 + " MT/s");
            System.out.println("Form factor: " + physicalMemory.getMemoryType());
        }

        // Số slots và phần cứng đã dùng
        System.out.println("Slots used: " + memory.getPhysicalMemory().size() + " of 2");

        // Hardware reserved memory có thể không có sẵn trực tiếp
        System.out.println("Hardware reserved: Not available in OSHI");
    }
}
