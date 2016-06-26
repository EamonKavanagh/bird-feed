package utils;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.util.serialization.DeserializationSchema;
import org.apache.flink.streaming.util.serialization.SerializationSchema;
import types.Bird;

public class BirdSchema implements DeserializationSchema<Bird>, SerializationSchema<Bird> {

    @Override
    public byte[] serialize(Bird bird) {
        return bird.toString().getBytes();
    }

    @Override
    public Bird deserialize(byte[] record) {
        return Bird.fromString(new String(record));
    }

    @Override
    public boolean isEndOfStream(Bird nextBird) {
        return false;
    }

    @Override
    public TypeInformation<Bird> getProducedType() {
        return TypeExtractor.getForClass(Bird.class);
    }
}
