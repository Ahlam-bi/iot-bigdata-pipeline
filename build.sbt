name := "IoT-BigData-Pipeline"
version := "1.0"
scalaVersion := "2.12.18"

val sparkVersion = "3.5.0"
val kafkaVersion = "3.5.1"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "org.apache.commons" % "commons-pool2" % "2.11.1",  // Ajout
  "org.apache.spark" %% "spark-token-provider-kafka-0-10" % sparkVersion 
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
  case PathList("META-INF", "maven", xs @ _*) => MergeStrategy.discard
  case PathList("META-INF", xs @ _*) => 
    xs.map(_.toLowerCase) match {
      case "manifest.mf" :: Nil | "index.list" :: Nil | "dependencies" :: Nil => MergeStrategy.discard
      case _ => MergeStrategy.discard
    }
  case "reference.conf" => MergeStrategy.concat
  case "application.conf" => MergeStrategy.concat
  case PathList("org", "apache", "spark", "unused", xs @ _*) => MergeStrategy.first
  case PathList("com", "google", xs @ _*) => MergeStrategy.first
  case PathList("org", "apache", "commons", xs @ _*) => MergeStrategy.first
  case PathList("module-info.class") => MergeStrategy.discard
  case x if x.endsWith(".proto") => MergeStrategy.first
  case x if x.contains("hadoop") => MergeStrategy.first
  case _ => MergeStrategy.first
}

assembly / assemblyJarName := "app.jar"

// Exclure les dépendances problématiques déjà présentes dans Spark
assembly / assemblyExcludedJars := {
  val cp = (assembly / fullClasspath).value
  cp.filter { f =>
    f.data.getName.contains("hadoop-client") ||
    f.data.getName.contains("hadoop-common") ||
    f.data.getName.contains("hadoop-hdfs")
  }
}