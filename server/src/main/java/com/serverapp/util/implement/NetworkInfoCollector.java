package com.serverapp.util.implement;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import com.serverapp.model.ClientCard;
import com.serverapp.util.INetworkInfoCollector;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;

public class NetworkInfoCollector implements INetworkInfoCollector {

    @Override
    public String getHostName() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostName();
        } catch (UnknownHostException e) {
            return "Unknown Host";
        }
    }

    @Override
    public String getIPv4Address() {
        NetworkIF networkIF = getActiveNetworkInterface();
        if (networkIF != null) {
            String[] ipv4s = networkIF.getIPv4addr();
            if (ipv4s.length > 0) {
                return ipv4s[0];
            }
        }
        return "No IPv4 Address";
    }

    @Override
    public String getMacAddress() {
        NetworkIF networkIF = getActiveNetworkInterface();
        if (networkIF != null) {
            return networkIF.getMacaddr();
        }
        return "No MAC Address";
    }

    private NetworkIF getActiveNetworkInterface() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        List<NetworkIF> networkIFs = hal.getNetworkIFs();

        for (NetworkIF net : networkIFs) {
            net.updateAttributes();
            if (net.getIPv4addr().length > 0 && !net.getName().contains("lo")) {
                return net;
            }
        }
        return null;
    }

    public String getHostName(NetworkIF networkIF) {
        try {
            InetAddress inetAddress = InetAddress.getByName(networkIF.getIPv4addr()[0]);
            return inetAddress.getHostName();
        } catch (UnknownHostException e) {
            return "Unknown Host";
        }
    }

    public List<ClientCard> getAllClientCardsInLAN() {
        List<ClientCard> clientCards = new ArrayList<>();
        String subnet = getSubnet();

        // Perform a network scan
        List<String> activeHosts = scanNetwork(subnet);

        for (String ip : activeHosts) {
            try {
                InetAddress address = InetAddress.getByName(ip);
                String hostName = address.getHostName();
                String macAddress = "MAC Address Unknown"; // MAC Address can be obtained with ARP, external tool or library
                String osVersion = "OS Version Unknown"; // OS Version is difficult to get remotely without special tools

                ClientCard clientCard = ClientCard.builder()
                        .hostName(hostName)
                        .ipAddress(ip)
                        .macAddress(macAddress)
                        .osVersion(osVersion)
                        .build();

                clientCards.add(clientCard);
            } catch (UnknownHostException e) {
                // Handle exception
                throw new RuntimeException("Error while getting host name");
            }
        }
        return clientCards;
    }

    private String getSubnet() {
        NetworkIF networkIF = getActiveNetworkInterface();
        if (networkIF != null) {
            String ip = networkIF.getIPv4addr()[0];
            return ip.substring(0, ip.lastIndexOf('.'));
        }
        return "192.168.1"; // Default subnet if not found
    }

    private List<String> scanNetwork(String subnet) {
        subnet = "192.168.1";
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

    private class NetworkScanTask implements Callable<List<String>> {
        private final String subnet;
        private final int start;
        private final int end;

        public NetworkScanTask(String subnet, int start, int end) {
            this.subnet = subnet;
            this.start = start;
            this.end = end;
        }

        @Override
        public List<String> call() {
            List<String> activeHosts = new ArrayList<>();
            for (int i = start; i <= end; i++) {
                String host = subnet + "." + i;
                try {
                    InetAddress address = InetAddress.getByName(host);
                    if (address.isReachable(100)) {
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