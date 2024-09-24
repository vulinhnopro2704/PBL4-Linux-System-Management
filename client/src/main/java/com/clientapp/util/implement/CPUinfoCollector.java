package com.clientapp.util.implement;

import com.sun.management.OperatingSystemMXBean;

import com.clientapp.model.CPUinfo;
import com.clientapp.util.ICPUinfoCollector;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import java.lang.management.ManagementFactory;

public class CPUinfoCollector implements ICPUinfoCollector {
    SystemInfo si;
    CentralProcessor processor;
    GlobalMemory memory;
    OperatingSystem os;

    public CPUinfoCollector(){
        si = new SystemInfo();
        processor = si.getHardware().getProcessor();
        memory = si.getHardware().getMemory();
        os = si.getOperatingSystem();
    }

    public CPUinfo getCPUinfo(){
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double Utilization = osBean.getSystemCpuLoad();
        double Speed = (processor.getProcessorIdentifier().getVendorFreq() / 1_000_000_000.0);
        int Socket =  processor.getPhysicalPackageCount();
        int Processes = os.getProcessCount();
        int Threads =  os.getThreadCount();
        int Cores = processor.getPhysicalProcessorCount();
        int LogicalProcessors = processor.getLogicalProcessorCount();
        String upTime = FormatUtil.formatElapsedSecs(os.getSystemUptime());
        CPUinfo cpuinfo = new CPUinfo(Utilization,Speed,Processes,Threads,Cores,Socket,LogicalProcessors,upTime);
        return cpuinfo;
    }
}
