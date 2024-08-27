pbl4/
├── ansible/
│   ├── inventory.ini
│   ├── playbook.yml
│   ├── ansible.cfg
├── server/
│   ├── Dockerfile
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   ├── com/
│   │   │   │   │   ├── myapp/
│   │   │   │   │   │   ├── MainServer.java
│   │   │   │   │   │   ├── controller/
│   │   │   │   │   │   │   ├── ServerController.java
│   │   │   │   │   │   ├── model/
│   │   │   │   │   │   │   ├── Server.java
│   │   │   │   │   │   ├── util/
│   │   │   │   │   │   │   ├── SSHConnection.java
│   │   │   │   │   │   ├── view/
│   │   │   │   │   │   │   ├── ServerView.fxml
│   │   ├── resources/
│   │   │   ├── css/
│   │   │   ├── images/
│   ├── scripts/
│   │   ├── start_server.sh
│   ├── pom.xml
├── client/
│   ├── Dockerfile
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   ├── com/
│   │   │   │   │   ├── myapp/
│   │   │   │   │   │   ├── MainClient.java
│   │   │   │   │   │   ├── controller/
│   │   │   │   │   │   │   ├── ClientController.java
│   │   │   │   │   │   ├── model/
│   │   │   │   │   │   │   ├── Client.java
│   │   │   │   │   │   ├── util/
│   │   │   │   │   │   ├── view/
│   │   │   │   │   │   │   ├── ClientView.fxml
│   │   ├── resources/
│   │   │   ├── css/
│   │   │   ├── images/
│   ├── scripts/
│   │   ├── start_client.sh
│   ├── pom.xml
├── docker/
│   ├── docker-compose.yml
├── start_services.sh
├── .gitignore
├── README.md
