package com.serverapp.service;

import java.io.IOException;

public interface IMemoryUsageServer {
    void start() throws IOException;
    void stop() throws IOException;
}
