package com.clientapp.util.implement;

import com.clientapp.model.ClientProcessDetail;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import java.util.ArrayList;
import java.util.List;

public class ClientProcessDetailCollector {

    private final SystemInfo systemInfo;
    private final OperatingSystem os;

    public ClientProcessDetailCollector() {
        // Khởi tạo đối tượng SystemInfo và lấy thông tin hệ điều hành
        this.systemInfo = new SystemInfo();
        this.os = systemInfo.getOperatingSystem();
    }

    // Phương thức để lấy danh sách các tiến trình đang chạy
    public List<ClientProcessDetail> collectProcessDetails() {
        List<ClientProcessDetail> processDetails = new ArrayList<>();

        // Lấy danh sách các tiến trình
        List<OSProcess> processes = os.getProcesses();

        for (OSProcess process : processes) {
            ClientProcessDetail detail = new ClientProcessDetail();
            detail.setProcessID(String.valueOf(process.getProcessID()));
            detail.setProcessName(process.getName());
            detail.setProcessPath(process.getPath());
            detail.setCPUusage(String.format("%.2f%%", 100d * process.getProcessCpuLoadCumulative()));
            detail.setRAMusage(FormatUtil.formatBytes(process.getResidentSetSize()));

            processDetails.add(detail);
        }
        return processDetails;
    }
}

