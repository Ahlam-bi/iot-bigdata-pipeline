@echo off
echo ==========================================
echo IoT Pipeline Monitoring
echo ==========================================
echo.

echo Kafka Topics:
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

echo.
echo HDFS Usage:
docker exec namenode hadoop fs -du -h /iot

echo.
echo Docker Containers:
docker-compose ps

echo.
echo ==========================================
pause