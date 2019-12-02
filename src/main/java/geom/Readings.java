package geom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Readings {
    private final ConcurrentLinkedQueue<Reading> readings;
    private final int capacity;
    private Reading lastReading;

    public Readings(int capacity) {
        readings = new ConcurrentLinkedQueue();
        this.capacity = capacity;
    }

    public int add(Reading r, long latency) {
        r.setLatency(latency);
        lastReading = r;
        readings.add(r);
        if (readings.size() > capacity) {
            readings.poll();
        }
        return readings.size();
    }

    public Reading getLastReading() {
        return lastReading;
    }

    public Reading get() {
        return readings.poll();
    }

    public List<int[]> readingData() {
        List<int[]> l = new ArrayList<>();
        Iterator<Reading> ite = readings.iterator();
        while (ite.hasNext()) {
            l.add(ite.next().getSeries());
        }
        return l;
    }

    public int capacity() {
        return capacity;
    }

    public int size() {
        return readings.size();
    }
}
