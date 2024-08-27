# Sử dụng image Ubuntu làm base image
FROM ubuntu:20.04

# Cập nhật package list và cài đặt các công cụ cơ bản
RUN apt-get update && \
    apt-get install -y \
    openjdk-17-jdk \
    openssh-client \
    iputils-ping \
    netcat \
    curl \
    wget \
    vim \
    rsync \
    tzdata \
    && rm -rf /var/lib/apt/lists/*

# Tạo thư mục cho JavaFX và cài đặt JavaFX SDK
RUN wget https://gluonhq.com/download/javafx-sdk-20.0.2-linux-x64_bin.tar.gz && \
    tar -xzf javafx-sdk-20.0.2-linux-x64_bin.tar.gz && \
    mv javafx-sdk-20.0.2 /opt/javafx && \
    rm javafx-sdk-20.0.2-linux-x64_bin.tar.gz

# Thiết lập biến môi trường cho JavaFX
ENV PATH="/opt/javafx/bin:${PATH}"
ENV JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
ENV JAVA_FX_HOME="/opt/javafx"

# Đặt múi giờ
ENV TZ=Asia/Ho_Chi_Minh
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Tạo người dùng cho client
RUN useradd -ms /bin/bash clientuser && echo 'clientuser:clientpassword' | chpasswd

# Đặt hostname cho client (có thể tùy chỉnh)
ARG HOSTNAME=client
RUN echo $HOSTNAME > /etc/hostname

# Cấu hình SSH client (ví dụ: disable host key checking)
RUN mkdir -p /home/clientuser/.ssh && \
    echo "Host *\n\tStrictHostKeyChecking no\n" > /home/clientuser/.ssh/config && \
    chown -R clientuser:clientuser /home/clientuser/.ssh

# Chuyển sang user clientuser
USER clientuser
WORKDIR /home/clientuser

# Lệnh khởi động container
CMD ["tail", "-f", "/dev/null"]
