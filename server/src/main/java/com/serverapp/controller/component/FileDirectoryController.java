package com.serverapp.controller.component;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class FileDirectoryController {

    // Xử lý sự kiện khi nhấn nút upload
    @FXML
    public void handleUploadFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file để tải lên");

        // Bộ lọc chỉ cho phép chọn các file cụ thể, nếu cần
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tất cả các file", "*.*"),
                new FileChooser.ExtensionFilter("File Text", "*.txt"),
                new FileChooser.ExtensionFilter("File Hình ảnh", "*.png", "*.jpg", "*.jpeg")
        );

        // Lấy cửa sổ hiện tại để làm chủ hộp thoại file
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        // Mở hộp thoại và lấy file người dùng chọn
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // Ở đây, bạn có thể xử lý file đã chọn (ví dụ: tải lên server, lưu vào hệ thống,...)
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("File đã chọn");
            alert.setHeaderText(null);
            alert.setContentText("Bạn đã chọn file: " + selectedFile.getAbsolutePath());
            alert.showAndWait();
        } else {
            // Nếu người dùng không chọn file nào
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Không có file được chọn");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn một file để tải lên.");
            alert.showAndWait();
        }
    }
}
