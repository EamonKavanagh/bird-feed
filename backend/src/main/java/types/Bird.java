package types;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Bird {

    public final String family;
    public final String name;
    public final String id;
    public final double lat;
    public final double lon;
    public final Date timestamp;

    private static Random random = new Random();

    public Bird(String family, String name, String id) {
        this.family = family;
        this.name = name;
        this.id = id;
        lat = randomLatitude();
        lon = randomLongitude();
        Calendar ct = Calendar.getInstance();
        // Delay data by a random (Gaussian) amount of seconds.  99.7% of data come within 3 seconds
        ct.add(Calendar.SECOND, (int) Math.round(Math.min(0, random.nextGaussian())));
        timestamp = ct.getTime();
    }

    private static double nextDoubleInRange(double lo, double hi) {
        return lo + (hi - lo)*random.nextDouble();
    }

    private static double randomLatitude() {
        return nextDoubleInRange(40.764145, 40.800410);
    }

    private static double randomLongitude() {
        return nextDoubleInRange(-73.981419, -73.949232);
    }

    public String toString() {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        return gson.toJson(this);
    }

    public static Bird fromString(String record) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        return gson.fromJson(record, Bird.class);
    }
}
