package com.clientapp.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CPUinfo {
    double Utilization;
    double Speed;
    int Processes;
    int Threads;
    int Cores;
    int Socket;
    int LogicalProcessors;
    String Uptime;

    public CPUinfo(double utilization, double speed, int processes, int threads, int cores, int socket, int logicalProcessors, String upTime) {
        this.Utilization = utilization;
        this.Speed = speed;
        this.Processes = processes;
        this.Threads = threads;
        this.Cores = cores;
        this.Socket = socket;
        this.LogicalProcessors = logicalProcessors;
        this.Uptime = upTime;
    }
}
