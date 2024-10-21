package com.serverapp.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import com.serverapp.model.ClientCard;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

public class NetworkInfoCollector {
    public List<ClientCard> getAllClientCardsInLAN() {
        List<ClientCard> clientCards = new ArrayList<>();
        String subnet = getWifiSubnet();

        // Perform a network scan
        List<String> activeHosts = scanNetwork(subnet);

        for (String ip : activeHosts) {
            try {
                InetAddress address = InetAddress.getByName(ip);
                String hostName = address.getHostName();
                String macAddress = "Unknown"; // MAC Address can be obtained with ARP, external tool or library
                String osVersion = "Unknow"; // OS Version is difficult to get remotely without special tools

                ClientCard clientCard = ClientCard.builder()
                        .hostName(hostName)
                        .ipAddress(ip)
                        .macAddress(macAddress)
                        .osVersion(osVersion)
                        .isConnect(false)
                        .build();

                clientCards.add(clientCard);
            } catch (UnknownHostException e) {
                // Handle exception
                throw new RuntimeException("Error while getting host name");
            }
        }
        return clientCards;
    }

    private String getWifiSubnet() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        List<NetworkIF> networkIFs = hal.getNetworkIFs();

        for (NetworkIF net : networkIFs) {
            net.updateAttributes();
            if (net.getIPv4addr().length > 0 && net.getIfOperStatus() == NetworkIF.IfOperStatus.UP && net.getName().toLowerCase().contains("w")) {
                String ip = net.getIPv4addr()[0];
                return ip.substring(0, ip.lastIndexOf('.'));
            }
        }
        return "192.168.1"; // Default subnet if not found
    }

    private List<String> scanNetwork(String subnet) {
        List<String> activeHosts = new ArrayList<>();
        int numProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numProcessors);

        List<Future<List<String>>> futures = new ArrayList<>();
        int range = 255 / numProcessors;

        for (int i = 0; i < numProcessors; i++) {
            int start = i * range + 1;
            int end = (i == numProcessors - 1) ? 254 : (i + 1) * range;
            futures.add(executor.submit(new NetworkScanTask(subnet, start, end)));
        }

        for (Future<List<String>> future : futures) {
            try {
                activeHosts.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        return activeHosts;
    }

    private record NetworkScanTask(String subnet, int start, int end) implements Callable<List<String>> {

        @Override
            public List<String> call() {
                List<String> activeHosts = new ArrayList<>();
                for (int i = start; i <= end; i++) {
                    String host = subnet + "." + i;
                    try {
                        InetAddress address = InetAddress.getByName(host);
                        if (address.isReachable(500)) {
                            activeHosts.add(host);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return activeHosts;
            }
        }
}