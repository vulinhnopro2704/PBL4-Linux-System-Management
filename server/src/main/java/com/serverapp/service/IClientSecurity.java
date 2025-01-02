package com.serverapp.service;

import com.serverapp.model.ClientFirewallRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.net.Socket;

public interface IClientSecurity {
    void initialize();
    void sendCommand(String command, TableView<ClientFirewallRow> tableClient, TextArea txtAreaTerminalLogs) throws Exception;
    void listenForResponse(Socket clientSocket);
    void close();
}
