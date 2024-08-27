# Sử dụng image Ubuntu làm base image
FROM ubuntu:20.04

# Cập nhật package list và cài đặt các công cụ cần thiết
RUN apt-get update && \
    apt-get install -y \
    openjdk-21-jdk \
    openssh-server \
    iputils-ping \
    netcat \
    curl \
    wget \
    unzip \
    vim \
    rsync \
    && rm -rf /var/lib/apt/lists/*

# Tạo thư mục để chứa các file SSH và chạy SSH daemon
RUN mkdir /var/run/sshd

# Đặt mật khẩu root cho SSH (có thể tùy chỉnh theo ý bạn)
RUN echo 'root:rootpassword' | chpasswd

# Cho phép đăng nhập SSH với tài khoản root
RUN sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config

# Cho phép đăng nhập bằng mật khẩu
RUN sed -i 's/PasswordAuthentication no/PasswordAuthentication yes/' /etc/ssh/sshd_config

# Xóa các tùy chọn DNS để cải thiện tốc độ kết nối SSH
RUN echo "UseDNS no" >> /etc/ssh/sshd_config

# Cài đặt JavaFX SDK
RUN wget https://download2.gluonhq.com/openjfx/22.0.2/openjfx-22.0.2_linux-x64_bin-sdk.zip && \
    unzip openjfx-22.0.2_linux-x64_bin-sdk.zip -d /opt/javafx && \
    rm openjfx-22.0.2_linux-x64_bin-sdk.zip

# Thiết lập biến môi trường cho JavaFX
ENV PATH="/opt/javafx/javafx-sdk-22.0.2/bin:${PATH}"
ENV JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
ENV JAVA_FX_HOME="/opt/javafx/javafx-sdk-22.0.2"

# Copy ứng dụng vào container
COPY target/server-1.0-SNAPSHOT.jar /app/your-app.jar

# Expose cổng 22 để SSH có thể kết nối
EXPOSE 22

# Tạo script để khởi động ứng dụng
RUN echo '#!/bin/bash\n\
java --module-path /opt/javafx/javafx-sdk-22.0.2/lib --add-modules javafx.controls,javafx.fxml -jar /app/your-app.jar' > /start_app.sh && \
    chmod +x /start_app.sh

# Chạy SSH daemon và ứng dụng JavaFX cùng nhau
CMD ["/bin/bash", "-c", "/usr/sbin/sshd -D & /start_app.sh"]
