package com.dynamiccarsharing.car.search.es;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DemoSwitchTest {

    private DemoSwitch demoSwitch;

    @BeforeEach
    void setUp() {
        demoSwitch = new DemoSwitch();
    }

    @Test
    @DisplayName("Should initialize with OK mode")
    void testInitialState() {
        assertEquals(DemoSwitch.Mode.OK, demoSwitch.get());
    }

    @Test
    @DisplayName("Should set and get FAIL mode")
    void testSetFailMode() {
        demoSwitch.set(DemoSwitch.Mode.FAIL);
        assertEquals(DemoSwitch.Mode.FAIL, demoSwitch.get());
    }

    @Test
    @DisplayName("Should set and get SLOW mode")
    void testSetSlowMode() {
        demoSwitch.set(DemoSwitch.Mode.SLOW);
        assertEquals(DemoSwitch.Mode.SLOW, demoSwitch.get());
    }

    @Test
    @DisplayName("Should set and get OK mode")
    void testSetOkMode() {
        demoSwitch.set(DemoSwitch.Mode.FAIL);
        demoSwitch.set(DemoSwitch.Mode.OK);
        assertEquals(DemoSwitch.Mode.OK, demoSwitch.get());
    }

    @Test
    @DisplayName("Should default to OK mode when setting null")
    void testSetNullMode() {
        demoSwitch.set(DemoSwitch.Mode.FAIL);
        demoSwitch.set(null);
        assertEquals(DemoSwitch.Mode.OK, demoSwitch.get());
    }
}