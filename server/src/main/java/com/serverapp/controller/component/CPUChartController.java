package com.serverapp.controller.component;

import com.serverapp.controller.view.ClientPerformanceController;
import com.serverapp.model.CPUinfo;
import com.serverapp.model.MemoryUsage;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class CPUChartController {
    @FXML
    private VBox CPUchartVBox;
    @FXML
    private GridPane CPUchartGridPane;
    @FXML
    private Label utilizationLabel;
    @FXML
    private Label processLabel;
    @FXML
    private Label upTimeLabel;
    @FXML
    private Label speedLabel;
    @FXML
    private Label threadLabel;
    @FXML
    private Label coreLabel;
    @FXML
    private Label socketLabel;
    @FXML
    private Label logicalProcessorsLabel;


    private ClientPerformanceController clientPerformanceController;
    private XYChart.Series<Number, Number> CPUSeries;
    private int timeCounter = 0;

    private LineChart<Number, Number> cpuChart;

    @FXML
    public void initialize() {
        clientPerformanceController = new ClientPerformanceController();
        setupCharts();
        // Add the memory chart initially
        CPUchartVBox.getChildren().clear();
        CPUchartVBox.getChildren().add(cpuChart);
        CPUchartVBox.getChildren().add(CPUchartGridPane);
    }

    private void setupCharts() {
        // CPU Chart setup
        NumberAxis cpuXAxis = new NumberAxis();
        NumberAxis cpuYAxis = new NumberAxis();
        cpuXAxis.setLabel("Time (seconds)");
        cpuYAxis.setLabel("% Utilization");

        cpuChart = new LineChart<>(cpuXAxis, cpuYAxis);
        cpuChart.setTitle("CPU Utilization");
        CPUSeries = new XYChart.Series<>();
        CPUSeries.setName("CPU");
        cpuChart.getData().add(CPUSeries);
    }

    public void updateCPUUsage(CPUinfo cpuInfo) {
        if (cpuInfo == null) {
            return;
        }
        double utilization = cpuInfo.getUtilization() * 100;
        double speed = cpuInfo.getSpeed();
        int processes = cpuInfo.getProcesses();
        int threads = cpuInfo.getThreads();
        int cores = cpuInfo.getCores();
        int socket = cpuInfo.getSocket();
        int logicalProcessors = cpuInfo.getLogicalProcessors();
        String uptime = cpuInfo.getUptime();

        // Update chart data
        CPUSeries.getData().add(new XYChart.Data<>(timeCounter++, utilization));
        if (CPUSeries.getData().size() > 60) {
            CPUSeries.getData().remove(0);
        }
        utilizationLabel.setText(String.format("Utilization: %.2f %%", utilization));
        processLabel.setText("Processes: " + processes);
        threadLabel.setText("Threads: " + threads);
        coreLabel.setText("Cores: " + cores);
        socketLabel.setText("Socket: " + socket);
        logicalProcessorsLabel.setText("Logical Processors: " + logicalProcessors);
        upTimeLabel.setText("Uptime: " + uptime);
        speedLabel.setText("Speed: " + speed + " GHz");
    }

}
