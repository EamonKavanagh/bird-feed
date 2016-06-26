import java.io.InputStreamReader

import com.esotericsoftware.yamlbeans.YamlReader
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import utils.BirdFeedConfigurations


object Batch extends App {

  def countsByName(df: DataFrame) = {
    df.groupBy("date", "family", "name").agg(count("id").alias("count"), countDistinct("id").alias("unique"))
  }

  // Requires result from countsByName
  def countsByFamily(df: DataFrame) = {
    df.groupBy("date", "family").agg(sum("count").alias("count"), sum("unique").alias("unique"))
  }

  // Requires result from countsByFamily
  def countsOverall(df: DataFrame) = {
    df.groupBy("date").agg(sum("count").alias("count"), sum("unique").alias("unique"))
  }

  val schema = StructType(
    StructField("family", StringType) ::
    StructField("name", StringType) ::
    StructField("id", StringType) ::
    StructField("lat", DoubleType) ::
    StructField("lon", DoubleType) ::
    StructField("timestamp", TimestampType) :: Nil
  )

  val reader: YamlReader = new YamlReader(
    new InputStreamReader(classOf[HDFSConsumer].getResourceAsStream("config.yml"))
  )
  val config: BirdFeedConfigurations = reader.read(classOf[BirdFeedConfigurations])

  val sparkConf = new SparkConf(true)
    .set("spark.cassandra.connection.host", config.CASSANDRA_DNS)

  val sc = new SparkContext(
    "spark://" + config.SPARK_MASTERIP + ":" + config.SPARK_PORT,
    "birdfeed",
    sparkConf
  )

  val sqlContext = new SQLContext(sc)

  val logs = sqlContext.read.schema(schema).json(
    "hdfs://" + config.HDFS_DNS + ":" + config.HDFS_PORT + config.HDFS_DATADIR + "/*/*/*/*.json"
  )

  val df = logs.withColumn("date", to_date(from_utc_timestamp(logs("timestamp"), "EST")))

  // Calculate nameCounts and cache for usage in other computations
  val nameCounts = countsByName(df)
  nameCounts.cache
  
  val familyCounts = countsByFamily(nameCounts)
  val overallCounts = countsOverall(familyCounts)

  nameCounts.drop("family").write
    .format("org.apache.spark.sql.cassandra")
    .options(Map(
      "table" -> "names",
      "keyspace" -> "birdfeed"))
    .mode(SaveMode.Append)
    .save()

  familyCounts.write
    .format("org.apache.spark.sql.cassandra")
    .options(Map(
      "table" -> "families",
      "keyspace" -> "birdfeed"))
    .mode(SaveMode.Append)
    .save()


  overallCounts.withColumn("year", year(overallCounts("date"))).write
    .format("org.apache.spark.sql.cassandra")
    .options(Map(
      "table" -> "overall",
      "keyspace" -> "birdfeed"))
    .mode(SaveMode.Append)
    .save()

  sc.stop()
}