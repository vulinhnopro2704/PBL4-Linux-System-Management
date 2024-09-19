package com.serverapp.controller.component;

import java.io.*;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
public class FileDirectoryController {

    private static final String SAVE_DIRECTORY = "D:/idea";

    // Tính năng nhập file
    @FXML
    public void handleUploadFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn tệp để tải lên");

        // Bộ lọc file (tùy chọn)
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tất cả các tệp", "*.*"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Hình ảnh", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            File destination = new File(SAVE_DIRECTORY + "/" + selectedFile.getName());

            try (FileInputStream fis = new FileInputStream(selectedFile);
                 FileOutputStream fos = new FileOutputStream(destination);
                 BufferedInputStream bis = new BufferedInputStream(fis);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                byte[] buffer = new byte[1024];
                int length;

                while ((length = bis.read(buffer)) > 0) {
                    bos.write(buffer, 0, length);
                }

                System.out.println("Tệp đã được tải lên thành công và lưu tại: " + destination.getPath());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
