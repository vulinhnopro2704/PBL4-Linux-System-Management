package com.clientapp.service.implement;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;

public class WatchDirectoryClamAVClient {
    private static final String WATCHED_DIR = System.getProperty("user.home") + "/Documents"; // Thư mục cần theo dõi
    TcpClient tcpClient;
    private static final String TCP_SERVER_HOST = "127.0.0.1"; // Địa chỉ TCP server
    private static final int TCP_SERVER_PORT = 12345;          // Cổng TCP server

    public WatchDirectoryClamAVClient() {
        tcpClient = new TcpClient(TCP_SERVER_HOST, TCP_SERVER_PORT);
    }

    public void run() {
        // Start a new thread for the initial ClamAV scan
        new Thread(() -> {
            try {
                scanDirectoryWithClamAV(Paths.get(WATCHED_DIR));
            } catch (IOException e) {
                System.err.println("Error scanning directory with ClamAV: " + e.getMessage());
            }
        }).start();

        // Start a new thread for listening to directory events
        new Thread(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                Path path = Paths.get(WATCHED_DIR);
                registerAllDirectories(path, watchService);

                System.out.println("Watching directory: " + WATCHED_DIR);

                while (true) {
                    WatchKey key;
                    try {
                        key = watchService.take();
                    } catch (InterruptedException e) {
                        System.err.println("Watcher interrupted: " + e.getMessage());
                        return;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            Path filePath = path.resolve((Path) event.context());
                            System.out.println("New file detected: " + filePath);

                            if (Files.isDirectory(filePath)) {
                                registerAllDirectories(filePath, watchService);
                            }

                            // Kiểm tra file với ClamAV
                            boolean isMalicious = scanFileWithClamAV(filePath);
                            if (isMalicious) {
                                handleMalwareDetected(filePath.toString());
                            }
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error setting up WatchService: " + e.getMessage());
            }
        }).start();
    }

    private static boolean scanFileWithClamAV(Path filePath) {
        ProcessBuilder processBuilder = new ProcessBuilder("clamscan", filePath.toString());
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                StringBuilder result = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                process.waitFor();

                System.out.println("ClamAV scan result: " + result);
                return result.toString().contains("FOUND"); // Trả về true nếu phát hiện mã độc
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error scanning file with ClamAV: " + e.getMessage());
            return false;
        }
    }

    private void scanDirectoryWithClamAV(Path directoryPath) throws IOException {
        Files.walk(directoryPath).filter(Files::isRegularFile).forEach(filePath -> {
            boolean isMalicious = scanFileWithClamAV(filePath);
            if (isMalicious) {
                handleMalwareDetected(filePath.toString());
            }
        });
    }

    private void handleMalwareDetected(String filePath) {
        System.out.println("Malicious file detected: " + filePath);
        tcpClient.sendMessage("Malicious file detected: " + filePath);
    }

    private static void registerAllDirectories(Path start, WatchService watchService) throws IOException {
        Files.walk(start).filter(Files::isDirectory).forEach(dir -> {
            try {
                dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            } catch (IOException e) {
                System.err.println("Error registering directory: " + dir);
            }
        });
    }

    private static class TcpClient {
        private final String host;
        private final int port;

        public TcpClient(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public void sendMessage(String message) {
            try (Socket socket = new Socket(host, port);
                 OutputStream output = socket.getOutputStream();
                 PrintWriter writer = new PrintWriter(output, true)) {

                writer.println(message);
                System.out.println("Message sent to server: " + message);

            } catch (IOException e) {
                System.err.println("Error sending message to server: " + e.getMessage());
            }
        }
    }
}