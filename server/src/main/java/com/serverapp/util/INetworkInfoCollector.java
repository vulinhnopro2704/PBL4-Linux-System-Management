package com.serverapp.util;

import com.serverapp.model.ClientCard;
import oshi.hardware.NetworkIF;

import java.util.List;

public interface INetworkInfoCollector {
    String getHostName();
    String getIPv4Address();
    String getMacAddress();
    List<ClientCard> getAllClientCardsInLAN();
    String getHostName(NetworkIF networkIF);
}