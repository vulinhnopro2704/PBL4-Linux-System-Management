package com.serverapp.service;

import java.io.IOException;

public interface IProcessDetailServer {
    void start() throws IOException;
    void stop() throws IOException;
}
