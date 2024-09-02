package com.clientapp;

import com.clientapp.util.ISystemInfoCollector;
import com.clientapp.util.implement.SystemInfoCollector;
import javafx.application.Application;
import javafx.stage.Stage;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.NetworkIF;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OperatingSystem.OSVersionInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class MainClient extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Example usage
        ISystemInfoCollector infoCollector = new SystemInfoCollector();

        // Get CPU Info
        System.out.println("=== CPU Information ===");
        System.out.println(infoCollector.getCpuInfo());
        System.out.println();

        // Get Memory Info
        System.out.println("=== Memory Information ===");
        System.out.println(infoCollector.getMemoryInfo());
        System.out.println();

        // Get OS Info
        System.out.println("=== OS Information ===");
        System.out.println(infoCollector.getOsInfo());
        System.out.println();

        // Get Network Info
        System.out.println("=== Network Information ===");
        for (String netInfo : infoCollector.getNetworkInfo()) {
            System.out.println(netInfo);
        }
        System.out.println();

        // Get Hostname
        System.out.println("=== Hostname ===");
        System.out.println(infoCollector.getHostnameInfo());
        System.out.println();

        // Get Disk Usage Info
        System.out.println("=== Disk Usage ===");
        for (String diskInfo : infoCollector.getDiskUsageInfo()) {
            System.out.println(diskInfo);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

