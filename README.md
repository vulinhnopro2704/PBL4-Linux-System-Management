pbl4/
├── docker/
│   ├── Dockerfile.client
│   ├── Dockerfile.server
│   ├── docker-compose.yml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/
│   │   │   │   ├── myapp/
│   │   │   │   │   ├── Main.java
│   │   │   │   │   ├── controller/
│   │   │   │   │   │   ├── ClientController.java
│   │   │   │   │   │   ├── ServerController.java
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Client.java
│   │   │   │   │   │   ├── Server.java
│   │   │   │   │   ├── util/
│   │   │   │   │   │   ├── SSHConnection.java
│   │   │   │   │   ├── view/
│   │   │   │   │   │   ├── MainView.fxml
│   │   │   │   │   │   ├── ClientView.fxml
│   │   │   │   │   │   ├── ServerView.fxml
│   ├── resources/
│   │   ├── css/
│   │   │   ├── styles.css
│   │   ├── images/
│   │   │   ├── logo.png
│   ├── test/
│   │   ├── java/
│   │   │   ├── com/
│   │   │   │   ├── myapp/
│   │   │   │   │   ├── MainTest.java
├── scripts/
│   ├── start_clients.sh
│   ├── start_server.sh
├── .gitignore
├── README.md
├── pom.xml