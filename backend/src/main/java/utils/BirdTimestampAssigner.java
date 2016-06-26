package utils;

import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;
import types.Bird;

public class BirdTimestampAssigner implements AssignerWithPeriodicWatermarks<Bird> {

    private final long maxOutOfOrderness = 3000; // 3 second max delay, from Gaussian
    private long currentMaxTimestamp;

    @Override
    public long extractTimestamp(Bird bird, long previousElementTimestamp) {
        long timestamp = bird.timestamp.getTime();
        currentMaxTimestamp = Math.max(timestamp, currentMaxTimestamp);
        return timestamp;
    }

    @Override
    public Watermark getCurrentWatermark() {
        // New watermark is delayed by maxOutOfOrderness
        return new Watermark(currentMaxTimestamp - maxOutOfOrderness);
    }
}
