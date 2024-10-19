package com.serverapp.service;

import com.serverapp.controller.view.MainSystemController;
import com.serverapp.controller.view.MainSystemController;

public interface ISystemMonitoring {
    void start();
    void stop();
    void setPort(int port);
    void setMainSystemController(MainSystemController mainSystemController);
}