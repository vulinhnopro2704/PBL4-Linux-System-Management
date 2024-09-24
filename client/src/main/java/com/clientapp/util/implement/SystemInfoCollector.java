package com.clientapp.util.implement;

import com.clientapp.model.ClientDetail;
import com.clientapp.model.ClientProcessDetail;
import com.clientapp.util.IConvertData;
import com.clientapp.util.ISystemInfoCollector;
import com.fasterxml.jackson.databind.ObjectMapper;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemInfoCollector implements ISystemInfoCollector {

    private final HardwareAbstractionLayer hardware;
    private final OperatingSystem os;
    private final ObjectMapper objectMapper;
    private final IConvertData convertData;

    /**
     * Retrieves the hostname of the current machine.
     *
     * @return The hostname as a string.
     */

    private static String getHostname() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostName();
        } catch (UnknownHostException e) {
            return "Unknown Host";
        }
    }

    public SystemInfoCollector() {
        SystemInfo systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.os = systemInfo.getOperatingSystem();
        this.objectMapper = new ObjectMapper();
        this.convertData = new ConvertData();
    }


    // Hàm lấy thông tin client
    public ClientDetail getClientDetail() {
        String hostName = getHostname();
        var network = hardware.getNetworkIFs(false).getFirst();
        String ipAddress = Arrays.toString(network.getIPv4addr());
        String macAddress = network.getMacaddr();
        String OSVersion = os.toString();
        String processor = hardware.getProcessor().getProcessorIdentifier().toString();
        Long ram = convertData.bytesToMB(hardware.getMemory().getTotal());

        List<OSFileStore> fileStores = os.getFileSystem().getFileStores();
        Long totalDisk = 0L;
        Long usedDisk = 0L;

        for (OSFileStore fs : fileStores) {
            long total = fs.getTotalSpace();
            long free = fs.getFreeSpace();
            long used = total - free;
            totalDisk += convertData.bytesToGB(total);
            usedDisk += convertData.bytesToGB(used);
        }

        // Tạo đối tượng ClientDetail không có processDetails (ban đầu là trống)
        return new ClientDetail(hostName, ipAddress, macAddress, OSVersion, processor, ram, usedDisk, totalDisk, new ArrayList<>());
    }
}
