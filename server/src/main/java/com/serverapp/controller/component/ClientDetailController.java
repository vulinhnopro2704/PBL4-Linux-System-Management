package com.serverapp.controller.component;

import com.serverapp.model.ClientDetail;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ClientDetailController {

    @FXML
    private Label txtHostname;
    @FXML
    private Label txtMacAddress;
    @FXML
    private Label txtIpAddress;
    @FXML
    private Label txtCpuModel;
    @FXML
    private Label txtOsVersion;
    @FXML
    private Label txtRam;
    @FXML
    private Label txtDisk;

    public void receiveClientDetail(ClientDetail clientDetail) {
        txtHostname.setText(clientDetail.getHostName());
        txtMacAddress.setText(clientDetail.getMacAddress());
        txtIpAddress.setText(clientDetail.getIpAddress());
        txtCpuModel.setText(clientDetail.getCpuModel());
        txtOsVersion.setText(clientDetail.getOsVersion());
        txtRam.setText(clientDetail.getRam() + " GB");
        txtDisk.setText(clientDetail.getUsedDisk() + "/" + clientDetail.getTotalDisk() + " GB");
    }
}