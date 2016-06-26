public class Run {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) throw new IllegalArgumentException("Must specify producer, hdfs, batch, trending, or nearme");

        switch (args[0]) {
            case "producer":
                Producer.main(args);
                break;
            case "hdfs":
                HDFSConsumer.main(args);
                break;
            case "batch":
                Batch.main(args);
                break;
            case "trending":
                Trending.main(args);
            case "nearme":
                NearMe.main(args);
                break;
            default:
                throw new IllegalArgumentException(
                        "Incorrent argument: " + args[0] + ", please use producer, hdfs, batch, trending, or nearme"
                );
        }
    }
}
