package test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Application {

    private static final int PORT = 12345; // Cổng server
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataOutputStream out;
    private ExecutorService executorService = Executors.newSingleThreadExecutor(); // Xử lý kết nối client

    public static void main(String[] args) {
        launch(args); // Khởi chạy JavaFX
    }

    @Override
    public void start(Stage primaryStage) {
        // Tạo giao diện
        primaryStage.setTitle("Server - File Sender");

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Button btnChooseFile = new Button("Chọn file để gửi");
        Label lblStatus = new Label("Trạng thái: Đang chờ client...");

        btnChooseFile.setOnAction(event -> {
            try {
                if (clientSocket == null || clientSocket.isClosed()) {
                    lblStatus.setText("Trạng thái: Chưa có client kết nối!");
                    return;
                }
                // Mở cửa sổ chọn file
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Chọn file để gửi");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("All Files", "*.*"),
                        new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
                );
                File selectedFile = fileChooser.showOpenDialog(primaryStage);

                if (selectedFile != null) {
                    lblStatus.setText("Đang gửi file: " + selectedFile.getName());
                    sendFile(selectedFile);
                    lblStatus.setText("Đã gửi file: " + selectedFile.getName());
                }
            } catch (Exception e) {
                lblStatus.setText("Lỗi: " + e.getMessage());
                e.printStackTrace();
            }
        });

        root.getChildren().addAll(btnChooseFile, lblStatus);

        Scene scene = new Scene(root, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Khởi chạy server trong luồng riêng
        startServer(lblStatus);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (serverSocket != null) {
            serverSocket.close();
        }
        if (clientSocket != null) {
            clientSocket.close();
        }
        executorService.shutdown();
    }

    private void startServer(Label lblStatus) {
        executorService.execute(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                lblStatus.setText("Trạng thái: Đang chờ client kết nối...");
                clientSocket = serverSocket.accept(); // Chờ client kết nối
                lblStatus.setText("Trạng thái: Client đã kết nối!");

                out = new DataOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                lblStatus.setText("Lỗi: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void sendFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            // Gửi tên file
            out.writeUTF(file.getName());

            // Gửi kích thước file
            out.writeLong(file.length());

            // Gửi nội dung file
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            out.flush();
            System.out.println("File sent: " + file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

