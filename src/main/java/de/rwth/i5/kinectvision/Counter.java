package de.rwth.i5.kinectvision;

import java.util.ArrayList;

public class Counter {
    public static long time = 0;
    private static ArrayList<Long> times = new ArrayList<>();

    public static void reset() {
        times.clear();
    }

    public static void start() {
        time = System.currentTimeMillis();
    }

    public static void stop() {
        times.add(System.currentTimeMillis() - time);
    }

    public static void print() {
        for (Long aLong : times) {
            System.out.println(aLong);
        }
    }
}
