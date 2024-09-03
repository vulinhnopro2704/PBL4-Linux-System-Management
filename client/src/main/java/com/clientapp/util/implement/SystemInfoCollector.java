package com.clientapp.util.implement;

import com.clientapp.util.IConvertData;
import com.clientapp.util.ISystemInfoCollector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OperatingSystem.OSVersionInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class SystemInfoCollector implements ISystemInfoCollector {

    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hardware;
    private final OperatingSystem os;
    private final ObjectMapper objectMapper;
    private IConvertData convertData;

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
        this.systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.os = systemInfo.getOperatingSystem();
        this.objectMapper = new ObjectMapper();
        this.convertData = new ConvertData();
    }

    @Override
    public String getSystemInfoAsJson() {
        try {
            ObjectNode rootNode = objectMapper.createObjectNode();

            rootNode.put("cpuInfo", getCpuInfo());
            rootNode.put("memoryInfo", getMemoryInfo());
            rootNode.put("osInfo", getOsInfo());
            rootNode.put("hostname", getHostname());
            rootNode.putPOJO("networkInfo", getNetworkInfo());
            rootNode.putPOJO("diskUsageInfo", getDiskUsageInfo());

            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    @Override
    public String getCpuInfo() {
        CentralProcessor processor = hardware.getProcessor();
        return String.format("CPU: %s, Physical Cores: %d, Logical Cores: %d",
                processor.getProcessorIdentifier().getName(),
                processor.getPhysicalProcessorCount(),
                processor.getLogicalProcessorCount());
    }

    @Override
    public String getMemoryInfo() {
        GlobalMemory memory = hardware.getMemory();
        return String.format("Total Memory: %d MB, Available Memory: %d MB",
                convertData.bytesToMB(memory.getTotal()), convertData.bytesToMB(memory.getAvailable()));
    }

    @Override
    public String getOsInfo() {
        OSVersionInfo versionInfo = os.getVersionInfo();
        return String.format("Operating System: %s, Family: %s, Manufacturer: %s, Version: %s, Build: %s, Code Name: %s",
                os, os.getFamily(), os.getManufacturer(), versionInfo.getVersion(), versionInfo.getBuildNumber(), versionInfo.getCodeName());
    }

    @Override
    public List<String> getNetworkInfo() {
        List<String> networkDetails = new ArrayList<>();
        List<NetworkIF> networkIFs = hardware.getNetworkIFs();
        for (NetworkIF net : networkIFs) {
            net.updateAttributes();

            boolean isUp = net.getIfAlias() != null && !net.getIfAlias().isEmpty();
            boolean isLoopback = net.getIfAlias() != null && net.getIfAlias().contains("Loopback");
            boolean hasIPv4 = net.getIPv4addr() != null && net.getIPv4addr().length > 0;

            if (isUp && !isLoopback && hasIPv4) {
                networkDetails.add(String.format("Interface: %s, MAC: %s, IPv4: %s, IPv6: %s",
                        net.getDisplayName(),
                        net.getMacaddr(),
                        String.join(", ", net.getIPv4addr()),
                        String.join(", ", net.getIPv6addr())));
            }
        }
        return networkDetails;
    }

    @Override
    public String getHostnameInfo() {
        return getHostname();
    }

    @Override
    public List<String> getDiskUsageInfo() {
        List<String> diskDetails = new ArrayList<>();
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fileStores = fileSystem.getFileStores();
        for (OSFileStore fs : fileStores) {
            long total = fs.getTotalSpace();
            long free = fs.getFreeSpace();
            long used = total - free;
            diskDetails.add(String.format("File System: %s, Total Space: %s GB, Used Space: %s GB, Free Space: %s GB",
                    fs.getMount(), convertData.bytesToGB(total), convertData.bytesToGB(used), convertData.bytesToGB(free)));
        }
        return diskDetails;
    }
}
