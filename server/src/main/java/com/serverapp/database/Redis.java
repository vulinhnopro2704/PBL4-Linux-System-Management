package com.serverapp.database;

import com.serverapp.model.ClientCard;
import com.serverapp.model.ClientDetail;
import com.serverapp.model.ClientProcess;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

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

    public static Redis getInstance(){
        if (_instance == null){
            _instance = new Redis();
        }
        return _instance;
    }

    /**
     Add Client to Map with:
     @param clientDetail Client data get via socket
     - Key is ip address, example: 192.168.1.234
     - Value is ClientDetail
     @return
     - Null if no client have the same ip address and port (if key is address)
     - Previous ClientDetail if this address+port already has value before (replace value)
     */
    public ClientDetail putClientDetail(String key, ClientDetail clientDetail){
        if (mapClientDetailView.get(key) == null)
            return mapClientDetailView.put(key, clientDetail);
        return null;
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

    public List<ClientCard> getAllClientCard(){
        var list = mapClientDetailView.values().stream().map(clientDetail -> new ClientCard(
                clientDetail.getHostName(),
                clientDetail.getIpAddress(),
                clientDetail.getMacAddress(),
                clientDetail.getOsVersion(),
                clientDetail.getIsConnect()
        ));
        return list.toList();
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
                            .build()
            );
        });
    }


    public boolean containsIp(String ip){
        return mapClientDetailView.containsKey(ip);
    }
}
