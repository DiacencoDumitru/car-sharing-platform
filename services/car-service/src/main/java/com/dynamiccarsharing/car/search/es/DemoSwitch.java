package com.dynamiccarsharing.car.search.es;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class DemoSwitch {
    public enum Mode { OK, FAIL, SLOW }

    private final AtomicReference<Mode> mode = new AtomicReference<>(Mode.OK);

    public Mode get() {
        return mode.get();
    }

    public void set(Mode newMode) {
        mode.set(newMode == null ? Mode.OK : newMode);
    }
}
