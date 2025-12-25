import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, ProducerConfig}
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties
import scala.util.Random
import java.time.Instant

case class SensorData(
  sensorId: String,
  timestamp: Long,
  temperature: Double,
  humidity: Double,
  powerConsumption: Double,
  region: String,
  status: String
)

object IoTDataProducer {
  
  def createProducer(): KafkaProducer[String, String] = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092") 
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(ProducerConfig.ACKS_CONFIG, "all")
    
    new KafkaProducer[String, String](props)
  }
  
  def generateSensorData(sensorId: String, region: String): String = {
    val random = new Random()
     // Température de base par région
    val baseTemp = region match {
      case "North" => 15.0
      case "South" => 25.0
      case "East" => 20.0
      case "West" => 18.0
      case _ => 20.0
    }
     // Variation gaussienne ± 5°C
    val temperature = baseTemp + random.nextGaussian() * 5
      // Humidité entre 40% et 80%
    val humidity = 40 + random.nextDouble() * 40
      // Consommation entre 100W et 500W
    val powerConsumption = 100 + random.nextDouble() * 400
      //5% d'anomalie
    val status = if (random.nextDouble() < 0.05) "WARNING" else "NORMAL"
      //Timestamp actuel
    val timestamp = Instant.now().toEpochMilli
      //Serialisation JSON
    s"""{"sensorId":"$sensorId","timestamp":$timestamp,"temperature":$temperature,"humidity":$humidity,"powerConsumption":$powerConsumption,"region":"$region","status":"$status"}"""
  }
  
  def main(args: Array[String]): Unit = {
    val producer = createProducer()
    val regions = Array("North", "South", "East", "West")
    val numSensors = 100
    val topic = "iot-sensors-raw"
    
    println(s"Starting IoT Data Producer - Topic: $topic")
    
    try {
      var messageCount = 0
      
      while (true) {
        (1 to numSensors).foreach { i =>
          val sensorId = f"SENSOR_$i%03d"
          val region = regions(Random.nextInt(regions.length))
          val jsonData = generateSensorData(sensorId, region)
          
          val record = new ProducerRecord[String, String](topic, sensorId, jsonData)
          producer.send(record)
          
          messageCount += 1
          if (messageCount % 1000 == 0) {
            println(s"Sent $messageCount messages")
          }
        }
        
        Thread.sleep(100)
      }
      
    } catch {
      case e: Exception =>
        println(s"Error: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      producer.close()
    }
  }
}