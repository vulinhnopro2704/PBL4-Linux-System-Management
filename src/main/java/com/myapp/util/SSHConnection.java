package com.myapp.util;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SSHConnection {

    private Session session;

    public void connect(String username, String password, String host, int port) {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            System.out.println("Connected to " + host);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("Disconnected");
        }
    }
}
