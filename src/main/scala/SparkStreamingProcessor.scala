import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._

object SparkStreamingProcessor {
  
  def main(args: Array[String]): Unit = {
    
    val spark = SparkSession.builder()
      .appName("IoT-Streaming-Processor")
      .config("spark.sql.shuffle.partitions", "4")
      .getOrCreate()
    
    spark.sparkContext.setLogLevel("WARN")
    
    import spark.implicits._
    
    println("=" * 80)
    println("Starting IoT Real-Time Stream Processor")
    println("=" * 80)
    
    val sensorSchema = new StructType()
      .add("sensorId", StringType)
      .add("timestamp", LongType)
      .add("temperature", DoubleType)
      .add("humidity", DoubleType)
      .add("powerConsumption", DoubleType)
      .add("region", StringType)
      .add("status", StringType)
    
    val rawStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "kafka:29092")
      .option("subscribe", "iot-sensors-raw")
      .option("startingOffsets", "earliest")
      .load()
    
    val parsedStream = rawStream
      .selectExpr("CAST(value AS STRING) as json") // Convertir bytes en string
      .select(from_json($"json", sensorSchema).as("data")) // Parser JSON
      .select("data.*") // Extraire les colonnes
      .withColumn("event_time", to_timestamp(from_unixtime($"timestamp" / 1000))) // Timestamp Unix → Timestamp SQL
      
    val windowedAggregations = parsedStream
      .withWatermark("event_time", "10 minutes")
      .groupBy(
        window($"event_time", "5 minutes"),
        $"region"
      )
      .agg(
        avg("temperature").as("avg_temperature"),
        max("temperature").as("max_temperature"),
        count("*").as("sensor_count")
      )
    
    val consoleQuery = windowedAggregations
      .writeStream
      .outputMode("append")
      .format("console")
      .option("truncate", false)
      .trigger(Trigger.ProcessingTime("30 seconds"))
      .start()
    
    val hdfsQuery = parsedStream
      .writeStream
      .outputMode("append")
      .format("parquet")
      .option("path", "hdfs://namenode:9000/iot/raw-data")
      .option("checkpointLocation", "/tmp/checkpoint/raw")
      .partitionBy("region")
      .trigger(Trigger.ProcessingTime("60 seconds"))
      .start()
    
    println("All streaming queries started successfully!")
    
    spark.streams.awaitAnyTermination()
  }
}