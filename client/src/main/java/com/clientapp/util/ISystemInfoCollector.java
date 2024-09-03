package com.clientapp.util;

import java.util.List;

public interface ISystemInfoCollector {

    /**
     * Retrieves information about the CPU.
     *
     * @return A string containing CPU details.
     */
    String getCpuInfo();

    /**
     * Retrieves information about the memory.
     *
     * @return A string containing memory details.
     */
    String getMemoryInfo();

    /**
     * Retrieves information about the operating system.
     *
     * @return A string containing OS details.
     */
    String getOsInfo();

    /**
     * Retrieves information about the network interfaces.
     *
     * @return A list of strings, each containing details of a network interface.
     */
    List<String> getNetworkInfo();

    /**
     * Retrieves the hostname of the current machine.
     *
     * @return The hostname as a string.
     */
    String getHostnameInfo();

    /**
     * Retrieves information about disk usage.
     *
     * @return A list of strings, each containing details of a disk.
     */
    List<String> getDiskUsageInfo();

    /**
     * Retrieves system information in JSON format.
     *
     * @return A string containing system information in JSON format.
     */
    String getSystemInfoAsJson();
}
