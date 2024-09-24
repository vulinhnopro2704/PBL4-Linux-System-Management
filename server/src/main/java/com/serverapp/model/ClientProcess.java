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
    public String processID;
    public String processName;
    public String processPath;
    public String CPUusage;
    public String RAMusage;
}
