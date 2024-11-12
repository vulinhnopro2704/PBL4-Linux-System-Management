package com.serverapp.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.awt.*;

public class PushNotification {

    public static void showInformationNotification(String title, String message) {
        showNotification(title, message, TrayIcon.MessageType.INFO);
    }

    public static void showWarningNotification(String title, String message) {
        showNotification(title, message, TrayIcon.MessageType.WARNING);
    }

    public static void showErrorNotification(String title, String message) {
        showNotification(title, message, TrayIcon.MessageType.ERROR);
    }

    public static void showUpdateNotification(String title, String message) {
        showNotification(title, message, TrayIcon.MessageType.NONE);
    }

    private static void showNotification(String title, String message, TrayIcon.MessageType messageType) {
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage("icon.png");

                TrayIcon trayIcon = new TrayIcon(image, "Notification");
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip("System Notification");
                tray.add(trayIcon);

                trayIcon.displayMessage(title, message, messageType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Platform.runLater(() -> {
                Notifications.create()
                        .title(title)
                        .text(message)
                        .position(Pos.BOTTOM_RIGHT)
                        .hideAfter(Duration.seconds(5))
                        .showWarning();
            });
        }
    }
}