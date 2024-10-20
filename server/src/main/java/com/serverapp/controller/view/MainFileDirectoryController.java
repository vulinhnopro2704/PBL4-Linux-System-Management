package com.serverapp.controller.view;

import com.serverapp.controller.IController;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.awt.*;

public class MainFileDirectoryController implements IController {
    @FXML private ImageView btnUpfile;

    @FXML
    private void handleUploadFile(MouseEvent event) {
        FileDialog dialog = new FileDialog((Frame) null, "Select File to Open");
        dialog.setMode(FileDialog.LOAD);
        dialog.setVisible(true);
        String file = dialog.getFile();
        if (file != null) {
            String path = dialog.getDirectory() + file;
            System.out.println(path);
        }
    }

    @Override
    public void initialize() {

    }

    @Override
    public void stop() {

    }
}
