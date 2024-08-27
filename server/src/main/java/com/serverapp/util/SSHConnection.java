package com.serverapp.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SSHConnection {

    private final Map<String, Session> sessions = new HashMap<>();
    private final String user;
    private final String password;

    // Constructor to initialize the user and password
    public SSHConnection(String user, String password) {
        this.user = user;
        this.password = password;
    }

    // Method to connect to an SSH server
    public void connect(String host) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        sessions.put(host, session);
    }

    // Method to execute a command on a specific SSH server
    public String executeCommand(String host, String command) throws JSchException, IOException {
        Session session = sessions.get(host);
        if (session == null || !session.isConnected()) {
            throw new JSchException("No active session for host: " + host);
        }

        ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        channelExec.setCommand(command);

        InputStream in = channelExec.getInputStream();
        channelExec.connect();

        StringBuilder result = new StringBuilder();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) > 0) {
            result.append(new String(buffer, 0, read));
        }

        channelExec.disconnect();
        return result.toString();
    }

    // Method to disconnect from all SSH servers
    public void disconnectAll() {
        for (Session session : sessions.values()) {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
        sessions.clear();
    }
}
