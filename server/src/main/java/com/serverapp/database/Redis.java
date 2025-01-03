package com.serverapp.database;

import com.serverapp.model.ClientCard;
import com.serverapp.model.ClientCommnandRow;
import com.serverapp.model.ClientDetail;
import com.serverapp.model.ClientProcess;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

public class Redis {
    private static Redis _instance;

    /**
     * Save Connected Client's information
     */
    @Getter
    private Map<String, ClientDetail> mapClientDetailView = new HashMap<String, ClientDetail>();
    @Getter
    @Setter
    private List<ClientProcess> clientProcessView = new ArrayList<ClientProcess>();

    private Map<String, String> mapClientConsoleLogs = new HashMap<String, String>();

    public static Redis getInstance(){
        if (_instance == null){
            _instance = new Redis();
        }
        return _instance;
    }

    /**
     * Add Client to Map with:
     *
     * @param clientDetail Client data get via socket
     *                     - Key is ip address, example: 192.168.1.234
     *                     - Value is ClientDetail
     */
    public void putClientDetail(String key, ClientDetail clientDetail){
        if (mapClientDetailView.get(key) == null || !mapClientDetailView.get(key).getIsConnect()) {
            mapClientDetailView.put(key, clientDetail);
        }
    }

    /**
     Add Client to Map with:
     @param key is address, example: 192.168.1.234
     - Key is ip address
     - Value is Client Detail
     @return
     - Null if no client have the same ip address (if key is address)
     - ClientDetail if this ip address already has value.
     */
    public ClientDetail getClientDetail(String key) {
        Set<String> keySet = mapClientDetailView.keySet();
        for (String keys : keySet) {
            System.out.println(keys);
        }
        return mapClientDetailView.get(key);
    }


    public ClientCard getClientCard(String key){
        ClientDetail clientDetail = mapClientDetailView.get(key);
        return new ClientCard(
                clientDetail.getHostName(),
                clientDetail.getIpAddress(),
                clientDetail.getMacAddress(),
                clientDetail.getOsVersion(),
                clientDetail.getIsConnect()
        );
    }

    public List<ClientCard> getAllClientCard() {
        var list = mapClientDetailView.values().stream()
                .sorted(Comparator.comparing(ClientDetail::getIpAddress))
                .sorted(Comparator.comparing(ClientDetail::getIsConnect).reversed())
                .map(clientDetail -> new ClientCard(
                        clientDetail.getHostName(),
                        clientDetail.getIpAddress(),
                        clientDetail.getMacAddress(),
                        clientDetail.getOsVersion(),
                        clientDetail.getIsConnect()
                ))
                .collect(Collectors.toList());
        return list;
    }

    public List<ClientDetail> getAllClientDetail(){
        var list = mapClientDetailView.values().stream().map(clientDetail -> new ClientDetail(
                clientDetail.getCpuModel(),
                clientDetail.getRam(),
                clientDetail.getUsedDisk(),
                clientDetail.getTotalDisk(),
                clientDetail.getProcessDetails()
        ));
        return list.toList();
    }

    public void putAllClientCard(List<ClientCard> list){
        list.stream().forEach(clientCard -> {
            putClientDetail(
                    String.join(":",
                            clientCard.getIpAddress()
                    ),
                    new ClientDetail().builder()
                            .hostName(clientCard.getHostName())
                            .ipAddress(clientCard.getIpAddress())
                            .macAddress(clientCard.getMacAddress())
                            .osVersion(clientCard.getOsVersion())
                            .isConnect(clientCard.getIsConnect() != null && clientCard.getIsConnect())
                            .build()
            );
        });
    }


    public boolean containsIp(String ip){
        return mapClientDetailView.containsKey(ip);
    }

    public String getConsoleLogs(String ip) {
        return mapClientConsoleLogs.get(ip);
    }

    public void appendConsoleLogs(String ip, String logs) {
        mapClientConsoleLogs.merge(ip, logs, (a, b) -> a + "\n" + b);
    }

    public ObservableList<ClientCommnandRow> getAllAvailableClient() {
        List<ClientCard> clientCards = Redis.getInstance().getAllClientCard();
        ObservableList<ClientCommnandRow> data = FXCollections.observableArrayList(
                clientCards.stream()
                        .filter(clientCard -> clientCard != null && Boolean.TRUE.equals(clientCard.getIsConnect()))
                        .map(clientCard -> new ClientCommnandRow(
                                false,
                                clientCard.getHostName(),
                                clientCard.getIpAddress(),
                                clientCard.getMacAddress()
                        ))
                        .collect(Collectors.toList())
        );
        return data;
    }


    public void clearConsoleLogs(String ip) {
        mapClientConsoleLogs.put(ip, "");
    }


    /**
     * For Detect Malware
     * */

    private Map<String, String> clientMalwareResponse = new HashMap<>();

    // Append Malware Response to Map
    public void appendMalwareResponse(String ip, String response) {
        String currentResponse = clientMalwareResponse.get(ip);
        if (currentResponse == null) {
            clientMalwareResponse.put(ip, response);
        } else {
            clientMalwareResponse.put(ip, currentResponse + "\n" + response);
        }
    }

    // Get Malware Response from Map
    public String getMalwareResponse(String ip) {
        return clientMalwareResponse.get(ip);
    }

    // Clear Malware Response from Map
    public void clearMalwareResponse(String ip) {
        clientMalwareResponse.put(ip, "");
    }

    public void disconnectClient(String ip) {
        mapClientDetailView.get(ip).setIsConnect(false);
    }
}
