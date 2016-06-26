import java.io.InputStreamReader
import java.net.{InetAddress, InetSocketAddress}
import java.util.Properties

import com.esotericsoftware.yamlbeans.YamlReader
import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09
import org.apache.flink.streaming.connectors.elasticsearch2.{ElasticsearchSink, ElasticsearchSinkFunction, RequestIndexer}
import org.elasticsearch.client.Requests

import types.Bird
import utils.{BirdSchema, BirdTimestampAssigner, BirdFeedConfigurations}


object NearMe extends App {

  val reader = new YamlReader(
    new InputStreamReader(classOf[HDFSConsumer].getResourceAsStream("config.yml"))
  )
  val config = reader.read(classOf[BirdFeedConfigurations])

  val in = NearMe.getClass.getResourceAsStream("consumer.properties")
  val properties = new Properties
  properties.load(in)

  // Setup stream execution environment
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

  // Construct consumer and stream using event time watermarking
  val consumer = new FlinkKafkaConsumer09[Bird](config.TOPIC, new BirdSchema(), properties)
  val stream = env.addSource(consumer).assignTimestampsAndWatermarks(new BirdTimestampAssigner())

  // Setup Elasticsearch connection
  val esConfig = new java.util.HashMap[String, String]
  esConfig.put("bulk.flush.interval.ms", config.ELASTIC_FLUSH)
  esConfig.put("cluster.name", config.ELASTIC_CLUSTER)

  val esTransports = new java.util.ArrayList[InetSocketAddress]
  config.ELASTIC_DNS.map(DNS =>
    esTransports.add(new InetSocketAddress(InetAddress.getByName(DNS), config.ELASTIC_PORT))
  )

  // Dump data to Elasticsearch
  stream.addSink(new ElasticsearchSink(
    esConfig,
    esTransports,
    new ElasticsearchSinkFunction[Bird] {
      def createIndexRequest(bird: Bird) = {
        val json = new java.util.HashMap[String, Any]
        json.put("name", bird.name)
        json.put("location", bird.lat + "," + bird.lon)
        json.put("timestamp", bird.timestamp)
        Requests.indexRequest.index(bird.family.toLowerCase).`type`("sighting").id(bird.id).source(json)
      }

      override def process(bird: Bird, ctx: RuntimeContext, indexer: RequestIndexer) = {
        indexer.add(createIndexRequest(bird))
      }
    }))

  env.execute("Running near me calculations, dumping data to Elasticsearch")
}
