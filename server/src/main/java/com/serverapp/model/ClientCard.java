package com.serverapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ClientCard {
    private String hostName;
    private String ipAddress;
    private String macAddress;
    private String osVersion;
    private Boolean isConnect = false;
}
