#!/bin/bash

# Đặt biến môi trường cho JavaFX
export PATH="/opt/javafx/bin:${PATH}"
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
export JAVA_FX_HOME="/opt/javafx"

# Chạy ứng dụng JavaFX
java --module-path ${JAVA_FX_HOME}/lib --add-modules javafx.controls,javafx.fxml -cp /home/serveruser/your-app.jar com.myapp.MainServer
