package com.clientapp.service;

public interface IScreenCaptureClient {
    void captureAndSendScreen() throws Exception;
    void closeConnection();
}
