#!/bin/bash

# Kiểm tra xem có phải là root không
if [ "$(id -u)" -ne "0" ]; then
  echo "Vui lòng chạy script này với quyền root hoặc sudo."
  exit 1
fi

# Điều hướng đến thư mục chứa docker-compose.yml
cd docker || { echo "Thư mục docker không tồn tại."; exit 1; }

# Khởi động Docker Compose để tạo và khởi động các container
echo "Khởi động các dịch vụ với Docker Compose..."
docker-compose up -d

# Kiểm tra trạng thái các container
echo "Kiểm tra trạng thái của các container..."

# Kiểm tra trạng thái của các container
status=$(docker-compose ps --services --filter "status=running" | wc -l)
total_services=$(docker-compose config --services | wc -l)

if [ "$status" -eq "$total_services" ]; then
  echo "Tất cả các container đã được khởi động thành công."
else
  echo "Lỗi: Một hoặc nhiều container không khởi động thành công."
  docker-compose ps
  exit 1
fi

# Xem logs để đảm bảo mọi thứ hoạt động bình thường
echo "Xem logs của các container để kiểm tra hoạt động..."
docker-compose logs -f
