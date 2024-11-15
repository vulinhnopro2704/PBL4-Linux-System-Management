package test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {
    private static boolean isRunning = false;

    public static void main(String[] args) {
        try (Socket socket = new Socket("127.0.0.1", 12345);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("Connected to server.");

            // Start listening for server messages
            new Thread(() -> listenToServer(in)).start();

            // Keep the client running
            while (true) {
                // You can send commands or interact with the server here
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void listenToServer(DataInputStream in) {
        try {
            while (true) {
                String message = in.readUTF();
                System.out.println("Server says: " + message);

                if ("requestType.File_Transfer".equals(message)) {
                    isRunning = true;
                    System.out.println("Ready to receive file...");
                    receiveFile(in, "received_file.txt");
                } else {
                    // Handle other server commands
                    System.out.println("Received other command from server: " + message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveFile(DataInputStream in, String filePath) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            // Read the file data
            while (in.available() > 0 && (bytesRead = in.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("File received and saved to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

