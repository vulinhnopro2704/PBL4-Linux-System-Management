package com.serverapp.controller.view;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.prefs.Preferences;
import java.io.IOException;

public class WhitelistController {

    @FXML
    private ComboBox<String> ipAccessComboBox;

    @FXML
    private AnchorPane customIpPane;

    @FXML
    private TextField startIpTextField;

    @FXML
    private TextField endIpTextField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private Stage currentStage; // The current stage

    private static final String ALLOW_ALL_OPTION = "Cho phép tất cả"; // ComboBox item for "allow all"
    private static final String DENY_ALL_OPTION = "Từ chối tất cả"; // ComboBox item for "deny all"
    private static final String CUSTOM_OPTION = "Tùy chọn"; // ComboBox item for "custom"

    // Preferences key to store ComboBox selection
    private static final String COMBO_BOX_SELECTION_KEY = "ipAccessComboBoxSelection";

    // Method to initialize the controller and handle the ComboBox logic
    @FXML
    private void initialize() {
        // Load previous selection from preferences
        loadComboBoxSelection();

        ipAccessComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (ALLOW_ALL_OPTION.equals(newValue)) {
                customIpPane.setVisible(false); // Hide IP range input fields when "Cho phép tất cả" is selected
            } else if (DENY_ALL_OPTION.equals(newValue)) {
                customIpPane.setVisible(false); // Hide IP range input fields when "Từ chối tất cả" is selected
            } else if (CUSTOM_OPTION.equals(newValue)) {
                customIpPane.setVisible(true); // Show IP range input fields for custom IP range
            }
        });

        // Handle save button click event
        saveButton.setOnAction(event -> {
            // Check for empty fields or invalid IP format when "Tùy chọn" is selected
            if (CUSTOM_OPTION.equals(ipAccessComboBox.getSelectionModel().getSelectedItem())) {
                String startIp = startIpTextField.getText().trim();
                String endIp = endIpTextField.getText().trim();

                if (startIp.isEmpty() || endIp.isEmpty()) {
                    showWarning("Cảnh báo", "Vui lòng điền đầy đủ dải IP.");
                    return; // Stop saving if any IP field is empty
                }

                // Validate IP format
                if (!isValidIp(startIp)) {
                    showWarning("Cảnh báo", "Định dạng IP bắt đầu không hợp lệ.");
                    return;
                }

                if (!isValidIp(endIp)) {
                    showWarning("Cảnh báo", "Định dạng IP kết thúc không hợp lệ.");
                    return;
                }
            }

            // Save the current ComboBox selection and execute the corresponding iptables command
            saveState();
            executeIptablesCommand();
            closeWindow();
        });

        // Handle cancel button click event
        cancelButton.setOnAction(event -> {
            closeWindow();
        });
    }

    // Method to set the stage (window) reference
    public void setStage(Stage stage) {
        this.currentStage = stage;
    }

    // Method to save the current state (ComboBox selection and IP fields)
    private void saveState() {
        // Save ComboBox selection
        String selectedIpAccess = ipAccessComboBox.getSelectionModel().getSelectedItem();

        // Save to preferences
        Preferences preferences = Preferences.userNodeForPackage(getClass());
        preferences.put(COMBO_BOX_SELECTION_KEY, selectedIpAccess);

        // Save other fields if necessary (optional)
        String startIp = startIpTextField.getText();
        String endIp = endIpTextField.getText();
        System.out.println("Saved IP Access: " + selectedIpAccess);
        System.out.println("Start IP: " + startIp);
        System.out.println("End IP: " + endIp);
    }

    private void loadComboBoxSelection() {
        Preferences preferences = Preferences.userNodeForPackage(getClass());
        String savedSelection = preferences.get(COMBO_BOX_SELECTION_KEY, ALLOW_ALL_OPTION); // Default to "Cho phép tất cả"

        ipAccessComboBox.getSelectionModel().select(savedSelection); // Set the saved selection in ComboBox
    }

    // Method to show a warning alert
    private void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to close the window
    private void closeWindow() {
        if (currentStage != null) {
            currentStage.close(); // Close the current stage (window)
        }
    }

    // Method to execute the iptables command based on ComboBox selection
    private void executeIptablesCommand() {
        String selectedOption = ipAccessComboBox.getSelectionModel().getSelectedItem();

        try {
            if (ALLOW_ALL_OPTION.equals(selectedOption)) {
                // Allow all IPs (Example: Accept all incoming traffic)
                System.out.println("Executing: iptables -A INPUT -j ACCEPT");
                Runtime.getRuntime().exec("iptables -A INPUT -j ACCEPT");
            } else if (DENY_ALL_OPTION.equals(selectedOption)) {
                // Deny all IPs (Example: Reject all incoming traffic)
                System.out.println("Executing: iptables -A INPUT -j DROP");
                Runtime.getRuntime().exec("iptables -A INPUT -j DROP");
            } else if (CUSTOM_OPTION.equals(selectedOption)) {
                String startIp = startIpTextField.getText().trim();
                String endIp = endIpTextField.getText().trim();
                // Example: Deny IP range (This example just shows how to add the IP range to iptables)
                System.out.println("Executing: iptables -A INPUT -s " + startIp + " -d " + endIp + " -j ACCEPT");
                Runtime.getRuntime().exec("iptables -A INPUT -s " + startIp + " -d " + endIp + " -j ACCEPT");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showWarning("Lỗi", "Không thể thực thi lệnh iptables.");
        }
    }

    // Method to validate IP format
    private boolean isValidIp(String ip) {
        String ipPattern = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$"; // Simple pattern to match IP format
        if (ip.matches(ipPattern)) {
            // Check if each octet is between 0 and 255
            String[] parts = ip.split("\\.");
            for (String part : parts) {
                try {
                    int num = Integer.parseInt(part);
                    if (num < 0 || num > 255) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
