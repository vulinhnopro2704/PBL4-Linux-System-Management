package com.serverapp.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CPUinfo {
    public double Utilization;
    public double Speed;
    public int Processes;
    public int Threads;
    public int Cores;
    public int Socket;
    public int LogicalProcessors;
    public String Uptime;

    @Override
    public String toString() {
        return "CPUinfo [Utilization=" + Utilization + ", Speed=" + Speed + ", Processes=" + Processes + ", Threads=" + Threads + ", Cores=" + Cores + ", Socket=" + Socket + ", LogicalProcessors=" + LogicalProcessors + "]";
    }
}
