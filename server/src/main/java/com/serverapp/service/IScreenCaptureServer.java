package com.serverapp.service;

import java.io.IOException;

public interface IScreenCaptureServer {
    void start() throws IOException;
    void stop() throws IOException;
}
