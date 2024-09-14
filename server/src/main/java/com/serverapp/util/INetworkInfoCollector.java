package com.serverapp.util;

import com.serverapp.model.ClientCard;
import oshi.hardware.NetworkIF;

import java.util.List;

public interface INetworkInfoCollector {
    List<ClientCard> getAllClientCardsInLAN();
}