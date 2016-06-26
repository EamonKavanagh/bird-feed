package utils;

public class BirdFeedConfigurations {

    // Producer/Consumer topic
    public String TOPIC;

    // Speed at which records are produced in milliseconds
    public int PRODUCTION_SPEED;

    // Time for how long the consumer waits for new data in milliseconds
    public long CONSUMER_TIMEOUT;

    // Size of window and slide in seconds
    public int WINDOW;
    public int SLIDE;

    public String HDFS_DATADIR;
    public String HDFS_DNS;
    public int HDFS_PORT;
    public long HDFS_MAXFILESIZE;

    public String CASSANDRA_DNS;

    public String SPARK_MASTERIP;
    public int SPARK_PORT;

    public String[] ELASTIC_DNS;
    public int ELASTIC_PORT;
    public String ELASTIC_FLUSH;
    public String ELASTIC_CLUSTER;
}
