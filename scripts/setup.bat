@echo off
echo ==========================================
echo IoT Big Data Pipeline - Setup Script
echo ==========================================

echo Starting Docker containers...
docker-compose up -d

echo Waiting for services to start (60s)...
timeout /t 60 /nobreak

echo Creating Kafka topics...
docker exec kafka kafka-topics --create --bootstrap-server localhost:9092 --topic iot-sensors-raw --partitions 4 --replication-factor 1 --if-not-exists

docker exec kafka kafka-topics --create --bootstrap-server localhost:9092 --topic iot-alerts --partitions 2 --replication-factor 1 --if-not-exists

echo Creating HDFS directories...
docker exec namenode hadoop fs -mkdir -p /iot/raw-data
docker exec namenode hadoop fs -mkdir -p /iot/aggregations

echo.
echo ==========================================
echo Setup completed successfully!
echo ==========================================
echo.
echo Web UIs:
echo   - HDFS:  http://localhost:9870
echo   - Spark: http://localhost:8080
echo.
pause