import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import com.datastax.driver.core.Cluster;

import com.esotericsoftware.yamlbeans.YamlReader;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.cassandra.CassandraSink;
import org.apache.flink.streaming.connectors.cassandra.ClusterBuilder;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;

import types.Bird;
import utils.BirdSchema;
import utils.BirdTimestampAssigner;
import utils.BirdFeedConfigurations;


public class Trending {

    public static void main(String[] args) throws Exception {

        YamlReader reader = new YamlReader(
                new InputStreamReader(Trending.class.getResourceAsStream("config.yml"))
        );
        final BirdFeedConfigurations birdFeedConfigurations = reader.read(BirdFeedConfigurations.class);

        // Construct window with default size of 1 minute and slide of 10 seconds
        Time window = Time.seconds(birdFeedConfigurations.WINDOW);
        Time slide = Time.seconds(birdFeedConfigurations.SLIDE);

        // Setup stream execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        InputStream in = HDFSConsumer.class.getResourceAsStream("consumer.properties");
        Properties properties = new Properties();
        properties.load(in);

        // Construct consumer and stream using event time watermarking
        FlinkKafkaConsumer09<Bird> consumer = new FlinkKafkaConsumer09<>(birdFeedConfigurations.TOPIC, new BirdSchema(), properties);
        SingleOutputStreamOperator<Bird> stream = env.addSource(consumer)
                .assignTimestampsAndWatermarks(new BirdTimestampAssigner());

        final String DNS = birdFeedConfigurations.CASSANDRA_DNS;
        // Aggregate data to reduce DB writes
        SingleOutputStreamOperator trending = stream
                .map(new MapFunction<Bird, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> map(Bird bird) throws Exception {
                        return new Tuple3<>(bird.family, bird.name, 1);
                    }
                }).keyBy(0, 1).timeWindow(window, slide).sum(2);

        // Dump data to Cassandra
        CassandraSink.addSink(trending)
                .setQuery("INSERT INTO birdfeed.trending (family, name, count) VALUES (?, ?, ?);")
                .setClusterBuilder(new ClusterBuilder() {
                    @Override
                    public Cluster buildCluster(Cluster.Builder builder) {
                        return builder.addContactPoint(DNS).build();
                    }
                })
                .build();

        env.execute("Running trending calculation, dumping data to Cassandra");
    }
}