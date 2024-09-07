package com.clientapp.model;

public class ClientDetail {
    public String hostName;
    public String ipAddress;
    public String macAddress;
    public String osVersion;
    public String cpuModel;
    public Long ram;
    public Long usedDisk;
    public Long totalDisk;
    public Boolean isConnect;

    public ClientDetail(
            String hostName, String ipAddress, String MACAddress,
            String OSVersion, String cpuModel, Long ram,
            Long usedDisk, Long totalDisk){
        this.hostName = hostName;
        this.ipAddress = ipAddress;
        this.macAddress = MACAddress;
        this.osVersion = OSVersion;
        this.cpuModel = cpuModel;
        this.ram = ram;
        this.usedDisk = usedDisk;
        this.totalDisk = totalDisk;
    }
}
