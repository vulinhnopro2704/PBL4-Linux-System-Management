package com.clientapp.util.implement;

import com.clientapp.model.ClientProcessDetail;
import com.clientapp.util.IClientProcessDetailCollector;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import java.util.ArrayList;
import java.util.List;

public class ClientProcessDetailCollector implements IClientProcessDetailCollector {

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

            // Kiểm tra nếu hệ điều hành là Windows để lấy đường dẫn tiến trình
            if (os.getFamily().toLowerCase().contains("windows")) {
                detail.setProcessPath(process.getPath() != null ? process.getPath() : "N/A");
            } else {
                detail.setProcessPath(process.getPath() != null ? process.getPath() : "Unknown");
            }

            // Lấy mức độ sử dụng CPU và kiểm tra nếu giá trị hợp lệ
            double cpuLoad = process.getProcessCpuLoadCumulative();
            if (cpuLoad >= 0) {
                detail.setCPUusage(String.format("%.2f%%", 100d * cpuLoad));
            } else {
                detail.setCPUusage("N/A");
            }

            // Lấy mức độ sử dụng RAM và chuyển đổi sang MB
            long ramUsageBytes = process.getResidentSetSize();
            double ramUsageMB = ramUsageBytes / (1024.0 * 1024.0);  // Chuyển từ byte sang MB
            detail.setRAMusage(String.format("%.1f MB", ramUsageMB));

            if (ramUsageMB > 20)
                processDetails.add(detail);
        }
        return processDetails;
    }
}
