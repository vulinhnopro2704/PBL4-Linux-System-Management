package com.serverapp.util;

import com.serverapp.controller.view.MainController;

public interface ISystemMonitoring {
    void start();
    void stop();
    void setPort(int port);
    void setMainController(MainController mainController);
}