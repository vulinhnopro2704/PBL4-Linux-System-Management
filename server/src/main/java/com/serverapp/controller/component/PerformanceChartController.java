package com.serverapp.controller.component;

import com.serverapp.controller.view.ClientPerformanceController;
import com.serverapp.model.CPUinfo;
import com.serverapp.model.MemoryUsage;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class PerformanceChartController {
    @FXML
    private VBox clientChartVBox;  // VBox containing AnchorPane and GridPane
    @FXML
    private AnchorPane chartAnchorPane;  // AnchorPane inside VBox for chart
    @FXML
    private GridPane grid;  // GridPane inside VBox for data
    @FXML
    private Label totalRamLabel;
    @FXML
    private Label inUseLabel;
    @FXML
    private Label availableLabel;
    @FXML
    private Label speedLabel;
    @FXML
    private Label typeOfRamLabel;
    @FXML
    private Label RamSlotUsedLabel;

    private ClientPerformanceController clientPerformanceController;
    private XYChart.Series<Number, Number> memoryUsageSeries;
    private XYChart.Series<Number, Number> CPUSeries;
    private int timeCounter = 0;

    private LineChart<Number, Number> memoryChart;
    private LineChart<Number, Number> cpuChart;

    @FXML
    public void initialize() {
        clientPerformanceController = new ClientPerformanceController();
        setupCharts();
        // Add the memory chart initially
        clientChartVBox.getChildren().clear();
        clientChartVBox.getChildren().add(memoryChart);
        clientChartVBox.getChildren().add(grid);
    }

    private void setupCharts() {
        // Memory Chart setup
        NumberAxis memoryXAxis = new NumberAxis();
        NumberAxis memoryYAxis = new NumberAxis();
        memoryXAxis.setLabel("Time (seconds)");
        memoryYAxis.setLabel("Memory Usage (GB)");

        memoryChart = new LineChart<>(memoryXAxis, memoryYAxis);
        memoryChart.setTitle("Memory Usage");
        memoryUsageSeries = new XYChart.Series<>();
        memoryUsageSeries.setName("Memory");
        memoryChart.getData().add(memoryUsageSeries);

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

    public void updateMemoryUsage(MemoryUsage memoryUsage) {
        if (memoryUsage == null) {
            return;
        }
        long usedMemorySize = memoryUsage.getUsedMemorySize();
        double usedMemorySizeInMB = (double) usedMemorySize / (1024);

        // Update chart data
        memoryUsageSeries.getData().add(new XYChart.Data<>(timeCounter++, usedMemorySizeInMB));
        if (memoryUsageSeries.getData().size() > 60) {
            memoryUsageSeries.getData().remove(0);
        }

        // Update GridPane data
        totalRamLabel.setText("Total Ram: " + memoryUsage.getTotalMemorySize() + " MB");
        inUseLabel.setText(String.format("In use: %.2f GB", usedMemorySizeInMB));
        availableLabel.setText("Available: " + (int) (memoryUsage.getFreeMemory()) + " MB");
        speedLabel.setText("Speed: " + memoryUsage.getSpeed() + " MT/s");
        typeOfRamLabel.setText("Type of RAM: " + memoryUsage.getRamType());
        RamSlotUsedLabel.setText("Slot Used: " + memoryUsage.getSlotUsed() + " of 2");
    }
}
