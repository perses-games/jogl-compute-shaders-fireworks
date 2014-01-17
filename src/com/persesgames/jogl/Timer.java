package com.persesgames.jogl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Date: 1/4/14
 * Time: 8:00 PM
 */
public class Timer {
    private final static Logger logger = LoggerFactory.getLogger(Timer.class);

    private Map<String, Long> start = new HashMap<>();
    private Map<String, Long> times = new HashMap<>();
    private Map<String, Long> calls = new HashMap<>();
    private long nanoStart = System.nanoTime();

    private final TimeUnit unit;
    private final int units;

    private long lastLog = System.nanoTime();

    public Timer(TimeUnit unit, int units) {
        this.unit = unit;
        this.units = units;
    }

    private long getStart(String timer) {
        Long result = start.get(timer);

        if (result == null) {
            result = System.nanoTime();
            start.put(timer, result);
        }

        return result;
    }

    private long getTime(String timer) {
        Long result = times.get(timer);

        if (result == null) {
            result = 0l;

            times.put(timer, result);
        }

        return result;
    }

    private long getCalls(String timer) {
        Long result = calls.get(timer);

        if (result == null) {
            result = 0l;

            calls.put(timer, result);
        }

        return result;
    }

    public void start(String timer) {
        start.put(timer, System.nanoTime());
    }

    public void stop(String timer) {
        long start = getStart(timer);

        long delta = System.nanoTime() - start;

        long time = getTime(timer);

        time += delta;

        times.put(timer, time);
        calls.put(timer, getCalls(timer) + 1);
    }

    public void reset() {
        lastLog = System.nanoTime();

        start.clear();
        times.clear();
        calls.clear();
    }

    public void log() {
        if (System.nanoTime() > (lastLog + unit.toNanos(units))) {

            for (String timer : times.keySet()) {
                logger.info("Timer '{}' calls '{}' time '{}ms' time/call '{}ms'", timer, getCalls(timer), (getTime(timer) / 1000000d), (getTime(timer) / 1000000d) / (double)getCalls(timer));
            }

            reset();
        }
    }




}
