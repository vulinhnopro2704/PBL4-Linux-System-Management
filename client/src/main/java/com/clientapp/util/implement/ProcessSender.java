package com.clientapp.util.implement;

import com.clientapp.model.ClientProcessDetail;
import com.google.gson.Gson;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ProcessSender {

    private final TCPClient tcpClient; // Sử dụng TCPClient
    private final ClientProcessDetailCollector collector = new ClientProcessDetailCollector();
    private final Gson gson = new Gson();

    public ProcessSender(TCPClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    // Hàm gửi dữ liệu process tới server qua TCP
    public void sendProcessDetails() {
        try {
            List<ClientProcessDetail> processDetails = collector.collectProcessDetails();
            String jsonProcessDetails = gson.toJson(processDetails);

            // Kiểm tra dữ liệu process
            System.out.println(jsonProcessDetails);

            // Gửi thông tin tiến trình tới server
            tcpClient.sendData(jsonProcessDetails);
            System.out.println("Process details sent to server.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm khởi chạy luồng để gửi dữ liệu liên tục
    public void startSendingProcessDetails(int intervalInMilliseconds) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendProcessDetails();
            }
        }, 0, intervalInMilliseconds);  // Gửi ngay lập tức, sau đó cứ sau một khoảng thời gian gửi lại.
    }
}
