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
public class ClientProcess {
    private String processID;
    private String processName;
    private String processPath;
    private String CPUusage;
    private String RAMusage;
}
