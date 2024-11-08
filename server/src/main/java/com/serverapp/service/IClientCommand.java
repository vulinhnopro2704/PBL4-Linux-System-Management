package com.serverapp.service;

import com.serverapp.model.ClientCommnandRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import java.net.Socket;


public interface IClientCommand {
    void initialize();
    void sendCommand(String command, TableView<ClientCommnandRow> tableClient, TextArea txtAreaTerminalLogs) throws Exception;
    void listenForResponse(Socket clientSocket);
    void close();
}
