package org.example;

import org.HdrHistogram.Histogram;
import org.HdrHistogram.Recorder;

import java.util.concurrent.atomic.LongAdder;

public class StatisticsAggregator {
    private final Recorder recorder = new Recorder(3);
    private final LongAdder successfulRequests = new LongAdder();
    private final LongAdder failedRequests = new LongAdder();

    public void recordSuccess(long latencyMillis) {
        if (latencyMillis < 0) latencyMillis = 0;
        recorder.recordValue(latencyMillis);
        successfulRequests.increment();
    }

    public void recordFailure() {
        failedRequests.increment();
    }

    public void printReport() {
        Histogram h = recorder.getIntervalHistogram();

        System.out.println("\n--- Load Test Report ---");
        System.out.printf("Successful Requests: %,d%n", successfulRequests.sum());
        System.out.printf("Failed Requests:     %,d%n", failedRequests.sum());

        if (h.getTotalCount() == 0) {
            System.out.println("No latency samples recorded.");
            System.out.println("------------------------");
            return;
        }

        System.out.println("\nResponse Time (milliseconds):");
        System.out.printf("  Count:  %,d%n", h.getTotalCount());
        System.out.printf("  Min:    %,d%n", h.getMinValue());
        System.out.printf("  Mean:   %.2f%n", h.getMean());
        System.out.printf("  p50:    %,d%n", h.getValueAtPercentile(50));
        System.out.printf("  p90:    %,d%n", h.getValueAtPercentile(90));
        System.out.printf("  p99:    %,d%n", h.getValueAtPercentile(99));
        System.out.printf("  p99.9:  %,d%n", h.getValueAtPercentile(99.9));
        System.out.printf("  Max:    %,d%n", h.getMaxValue());
        System.out.println("------------------------");
    }
}