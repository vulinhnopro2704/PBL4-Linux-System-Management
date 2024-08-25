package com.myapp.util;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SSHConnection {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 2222;  // Sửa port thành 2222
        String user = "root";
        String password = "rootpassword";

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);

            session.setConfig("StrictHostKeyChecking", "no");

            session.connect();
            System.out.println("Kết nối thành công!");
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
