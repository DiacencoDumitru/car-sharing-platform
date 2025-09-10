package org.example;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.OptionalLong;

final class Bench {

    record Result(String name, Duration elapsed, Map<Character, Long> counts, long totalLetters, boolean suspectedIncorrect, OptionalLong retries) {}

    static Result time(String name, RunnableWithResult r) {
        Instant t0 = Instant.now();
        var out = r.run();
        Instant t1 = Instant.now();
        return new Result(name, Duration.between(t0, t1), out.counts(), out.totalLetters(), out.suspectedIncorrect(), out.retries());
    }

    interface RunnableWithResult {
        Output run();
    }

    record Output(Map<Character, Long> counts, long totalLetters, boolean suspectedIncorrect, OptionalLong retries) {
        Output(Map<Character, Long> counts, long totalLetters, boolean suspectedIncorrect) {
            this(counts, totalLetters, suspectedIncorrect, OptionalLong.empty());
        }
    }
}