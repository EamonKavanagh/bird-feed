import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;

import com.esotericsoftware.yamlbeans.YamlReader;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import types.Bird;
import utils.BirdFeedConfigurations;


public class Producer {

    private static KafkaProducer<String, String> producer;
    private static String[] data;

    private static Random random = new Random();
    private static int mean;
    private static int stddev;

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static void initializeProducer() throws IOException {
        InputStream in = Producer.class.getResourceAsStream("producer.properties");
        Properties properties = new Properties();
        properties.load(in);
        producer = new KafkaProducer<>(properties);
    }

    private static void loadData() throws IOException{
        LinkedList<String> tmp = new LinkedList<>();

        InputStreamReader in = new InputStreamReader(Producer.class.getResourceAsStream("data.csv"));
        BufferedReader reader = new BufferedReader(in);

        // Discard header, load rest of file
        reader.readLine();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            tmp.add(line);
        }

        data = tmp.toArray(new String[tmp.size()]);
    }

    private static void publishMessage(String message, String topic) {
        producer.send(new ProducerRecord<String, String>(topic, message));
    }

    private static String buildRecord() {
        // Randomly sample data to generate record using normal distribution
        int ind = (int) (random.nextGaussian()*stddev + mean);
        String[] selection = data[Math.max(0, Math.min(data.length-1, ind))].split(",");

        // Construct types.Bird object which will generate lat/lon and timestamp
        Bird bird = new Bird(selection[0], selection[1], selection[2]);

        return bird.toString();
    }

    // Linear time shuffle to reorder data so it's more dynamic
    private static void shuffleData() {
        for (int i = data.length-1; i > 0; i--) {
            int index = random.nextInt(i+1);
            String exch = data[index];
            data[index] = data[i];
            data[i] = exch;
        }
    }

    public static void main(String[] args) throws IOException {
        initializeProducer();
        loadData();
        mean = data.length/2;
        stddev = data.length/6;

        YamlReader reader = new YamlReader(
                new InputStreamReader(HDFSConsumer.class.getResourceAsStream("config.yml"))
        );
        final BirdFeedConfigurations birdFeedConfigurations = reader.read(BirdFeedConfigurations.class);

        final Runnable producer = new Runnable() {
            int ct = 0;
            public void run() {
                publishMessage(buildRecord(), birdFeedConfigurations.TOPIC);
                ct++;
                // Shuffle data every 100,000 iterations
                if (ct > 100000) {
                    shuffleData();
                    ct = 0;
                }
            }
        };

        scheduler.scheduleAtFixedRate(producer, 0, birdFeedConfigurations.PRODUCTION_SPEED, MILLISECONDS);
    }
}
