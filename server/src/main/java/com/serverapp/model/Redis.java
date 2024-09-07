package com.serverapp.model;

import lombok.Getter;

import java.util.Map;

public class Redis {
    @Getter
    private static Redis _instance;

    public Redis getInstance(){
        if (_instance == null){
            _instance = new Redis();
        }
        return _instance;
    }

    /**
     * Save Connected Client's information
    */
    private Map<String, ClientDetail> mapClientDetailView;

    public ClientCard getClientCard(String key){
        ClientDetail clientDetail = mapClientDetailView.get(key);
        return new ClientCard(
                clientDetail.getHostName(),
                clientDetail.getIpAddress(),
                clientDetail.getMacAddress(),
                clientDetail.getOsVersion()
        );
    }
    /**
        Add Client to Map with:
        @param clientDetail Client data get via socket
        - Key is String include ip address + port, example: 192.168.1.234:8080
        - Value is ClientDetail
        @return
        - Null if no client have the same ip address and port (if key is address+port)
        - Previous ClientDetail if this address+port already has value before (replace value)
     */
    public ClientDetail putClientDetail(String key, ClientDetail clientDetail){
        return mapClientDetailView.put(key, clientDetail);
    }

    /**
     Add Client to Map with:
     @param key is address + port by String, example: 192.168.1.234:8080
     - Key is String include ip address + port
     - Value is Client Detail
     @return
     - Null if no client have the same ip address and port (if key is address+port)
     - ClientDetail if this address+port already has value.
     */
    public ClientDetail getClientDetail(String key) {
        return mapClientDetailView.get(key);
    }
}
