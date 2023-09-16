SERVER_JAR_NAME=server.jar
CLIENT_JAR_NAME=client.jar

build:
	mvn clean package
run-server:
	java -jar target/$(SERVER_JAR_NAME) 127.0.0.1 8888
run-client:
	java -jar target/$(CLIENT_JAR_NAME) $(id) 127.0.0.1 8888
test1:
	java -jar target/$(SERVER_JAR_NAME) 127.0.0.1 8888 &
	sleep 1
	java -jar target/$(CLIENT_JAR_NAME) a 127.0.0.1 8888 &
	java -jar target/$(CLIENT_JAR_NAME) b 127.0.0.1 8888 &