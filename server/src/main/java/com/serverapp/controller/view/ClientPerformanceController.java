package com.serverapp.controller.view;

import com.serverapp.controller.IController;
import com.serverapp.controller.component.CPUChartController;
import com.serverapp.controller.component.PerformanceChartController;
import com.serverapp.enums.RequestType;
import com.serverapp.model.CPUinfo;
import com.serverapp.model.MemoryUsage;
import com.serverapp.service.implement.CPUinfoServer;
import com.serverapp.service.implement.MemoryUsageServer;
import com.serverapp.socket.SocketManager;
import com.serverapp.util.CurrentType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import lombok.Setter;

import java.io.IOException;

import static com.serverapp.enums.RequestType.*;

public class ClientPerformanceController implements IController {

    @FXML
    private Label btnGeneral;

    @FXML
    private Label btnProcess;

    @FXML
    private Label btnPerformance;

    @FXML
    private Label btnScreen;

    @FXML
    private TabPane clientChartIncludeTabPane;

    @FXML
    private Tab performanceTab;

    @FXML
    private Tab CPUtab;

    MemoryUsageServer memoryUsageServer;
    CPUinfoServer cpuinfoServer;

    private PerformanceChartController performanceChartController;
    private CPUChartController cpuChartController;
    private String currentClientIp;
    private MemoryUsage memoryUsage;
    private CPUinfo cpuInfo;

    public void setCPUinfo(CPUinfo cpuInfo) {
        this.cpuInfo = cpuInfo;
    }
    @Setter
    private String fxmlPath = "/view/client-performance.fxml";

    public void setMemoryUsage(com.serverapp.model.MemoryUsage memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public MemoryUsage getMemoryUsage() {
        return memoryUsage;
    }

    @FXML
    public void initialize() throws IOException {
        System.out.println("Client Process Controller run");
        CurrentType.getInstance().setType(RequestType.PERFORMANCE_INFO);
        currentClientIp = AppController.getInstance().getCurrentClientIp();
        SocketManager.getInstance().sendCurrentRequestType(currentClientIp);
        addClientChart();
        memoryUsageServer = new MemoryUsageServer(this);
        memoryUsageServer.start();
        // Set listeners for tab selection changes
        performanceTab.setOnSelectionChanged(_ -> {
            if (performanceTab.isSelected()) {  // Check if tab is selected, not deselected
                try {
                    performaceChartStart();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        CPUtab.setOnSelectionChanged(_ -> {
            if (CPUtab.isSelected()) {  // Check if tab is selected, not deselected
                try {
                    CPUchartStart();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void performaceChartStart() throws IOException {
        if (cpuinfoServer != null){
            cpuinfoServer.stop();
            System.out.println("cpu info server stopped");
            cpuinfoServer = null;
        }
        CurrentType.getInstance().setType(PERFORMANCE_INFO);
        currentClientIp = AppController.getInstance().getCurrentClientIp();
        SocketManager.getInstance().sendCurrentRequestType(currentClientIp);
        memoryUsageServer = new MemoryUsageServer(this);
        memoryUsageServer.start();
    }

    public void CPUchartStart() throws IOException {
        if (memoryUsageServer != null){
            memoryUsageServer.stop();
            memoryUsageServer = null;
        }

        CurrentType.getInstance().setType(CPU_INFO);
        currentClientIp = AppController.getInstance().getCurrentClientIp();
        SocketManager.getInstance().sendCurrentRequestType(currentClientIp);
        cpuinfoServer = new CPUinfoServer(this);
        cpuinfoServer.start();
    }


    @Override
    public void update() {
        Platform.runLater(() -> {
            switch (CurrentType.getInstance().getType()){
                case CPU_INFO:
                    if (cpuInfo.getUtilization() != 0)
                        cpuChartController.updateCPUUsage(cpuInfo);
                    break;
                case PERFORMANCE_INFO:
                    if (memoryUsage.getUsedMemorySize() != 0)
                        performanceChartController.updateMemoryUsage(memoryUsage);
                    break;
                default:
            }
        });
        System.out.println("Update complete");
    }

    @Override
    public void stop() throws IOException {
        if (memoryUsageServer != null)
            memoryUsageServer.stop();
        if (cpuinfoServer != null)
            cpuinfoServer.stop();
    }

    @FXML
    public void viewchange() {
        btnGeneral.setOnMouseClicked(event -> {
            if (fxmlPath != null && !fxmlPath.isEmpty() && !fxmlPath.equals("/view/client-general.fxml")) {
                setFxmlPath("/view/client-general.fxml");
                loadPage(fxmlPath);
            }
        });

        btnProcess.setOnMouseClicked(event -> {
            if (fxmlPath != null && !fxmlPath.isEmpty() && !fxmlPath.equals("/view/client-process.fxml")) {
                setFxmlPath("/view/client-process.fxml");
                loadPage(fxmlPath);
            }
        });

        btnPerformance.setOnMouseClicked(event -> {
            if (fxmlPath != null && !fxmlPath.isEmpty() && !fxmlPath.equals("/view/client-performance.fxml")) {
                setFxmlPath("/view/client-performance.fxml");
                loadPage(fxmlPath);
            }
        });

        btnScreen.setOnMouseClicked(event -> {
            if (fxmlPath != null && !fxmlPath.isEmpty() && !fxmlPath.equals("/view/client-screen.fxml")) {
                setFxmlPath("/view/client-screen.fxml");
                loadPage(fxmlPath);
            }
        });
    }

    private void loadPage(String fxmlPath) {
        AppController.getInstance().loadPage(fxmlPath);
    }

    public void addClientChart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/performance-chart.fxml"));
            FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/component/cpu-chart.fxml"));
            Parent clientChartView = loader.load();
            Parent cpuChartView = loader2.load();
            performanceChartController = loader.getController();
            cpuChartController = loader2.getController();

            // Tạo một tab mới cho biểu đồ bộ nhớ
            performanceTab.setContent(clientChartView);
            CPUtab.setContent(cpuChartView);

            // TabPane
            clientChartIncludeTabPane.getTabs().clear();
            clientChartIncludeTabPane.getTabs().addAll(performanceTab, CPUtab);
        } catch (IOException e) {
            e.printStackTrace(); // Để xem chi tiết lỗi nếu không tải được FXML
        }
    }
}
