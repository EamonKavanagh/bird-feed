import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.Collections;
import java.util.Properties;

import com.esotericsoftware.yamlbeans.YamlReader;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import utils.BirdFeedConfigurations;
import utils.HDFSWriter;


public class HDFSConsumer {

    private static KafkaConsumer<String, String> consumer;

    private static void initializeConsumer() throws IOException {
        InputStream in = HDFSConsumer.class.getResourceAsStream("consumer.properties");
        Properties properties = new Properties();
        properties.load(in);
        consumer = new KafkaConsumer<>(properties);
    }

    public static void main(String[] args) throws IOException {
        initializeConsumer();


        YamlReader reader = new YamlReader(
                new InputStreamReader(HDFSConsumer.class.getResourceAsStream("config.yml"))
        );
        BirdFeedConfigurations birdFeedConfigurations = reader.read(BirdFeedConfigurations.class);
        HDFSWriter writer = new HDFSWriter(
                birdFeedConfigurations.HDFS_DATADIR,
                birdFeedConfigurations.HDFS_DNS,
                birdFeedConfigurations.HDFS_PORT,
                birdFeedConfigurations.HDFS_MAXFILESIZE
        );
        consumer.subscribe(Collections.singletonList(birdFeedConfigurations.TOPIC));

        while(true) {
            ConsumerRecords<String, String> records = consumer.poll(birdFeedConfigurations.CONSUMER_TIMEOUT);
            if (!records.isEmpty()) {
                for (ConsumerRecord<String, String> record : records) {
                    writer.write(record.value());
                }
            }
        }
    }

}
