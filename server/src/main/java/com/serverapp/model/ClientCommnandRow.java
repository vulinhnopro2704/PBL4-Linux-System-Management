package com.serverapp.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class ClientCommnandRow {
    private final SimpleStringProperty desktopName;
    private final SimpleStringProperty ipAddress;
    private final SimpleStringProperty macAddress;
    private final SimpleBooleanProperty checkbox;

    public ClientCommnandRow(boolean checkbox, String desktopName, String ipAddress, String macAddress) {
        this.checkbox = new SimpleBooleanProperty(checkbox);
        this.desktopName = new SimpleStringProperty(desktopName);
        this.ipAddress = new SimpleStringProperty(ipAddress);
        this.macAddress = new SimpleStringProperty(macAddress);
    }

    public String getDesktopName() {
        return desktopName.get();
    }

    public SimpleStringProperty desktopNameProperty() {
        return desktopName;
    }

    public String getIpAddress() {
        return ipAddress.get();
    }

    public SimpleStringProperty ipAddressProperty() {
        return ipAddress;
    }

    public String getMacAddress() {
        return macAddress.get();
    }

    public SimpleStringProperty macAddressProperty() {
        return macAddress;
    }

    public boolean isCheckbox() {
        return checkbox.get();
    }

    public SimpleBooleanProperty checkboxProperty() {
        return checkbox;
    }

    public void setCheckbox(boolean checkbox) {
        this.checkbox.set(checkbox);
    }
}