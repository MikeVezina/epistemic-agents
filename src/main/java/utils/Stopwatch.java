package utils;

public class Stopwatch {
    private long startTime;
    private Stopwatch()
    {
        startTime = System.nanoTime();
    }

    public long stop()
    {
        return System.nanoTime() - startTime;
    }

    public long stopMS()
    {
        return (System.nanoTime() - startTime) / 1000000;
    }

    public static Stopwatch startTiming()
    {
        return new Stopwatch();
    }
}
