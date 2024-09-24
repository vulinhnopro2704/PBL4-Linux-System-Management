package com.clientapp.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientProcessDetail {
    private String processID;
    private String processName;
    private String processPath;
    private String CPUusage;
    private String RAMusage;
}
