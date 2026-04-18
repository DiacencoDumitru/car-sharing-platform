package com.dynamiccarsharing.car.search.es;

import com.dynamiccarsharing.car.config.SecurityConfig;
import com.dynamiccarsharing.car.web.ResilienceDemoController;
import com.dynamiccarsharing.util.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResilienceDemoController.class)
@Import(SecurityConfig.class)
class ResilienceDemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DemoSwitch demoSwitch;

    @MockBean
    private CarSearchService carSearchService;

    @MockBean
    private CircuitBreakerFactory<?, ?> cbFactory;
    
    @MockBean
    private CircuitBreaker circuitBreaker;

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        when(cbFactory.create(anyString())).thenReturn(circuitBreaker);

        when(circuitBreaker.run(any(Supplier.class), any(Function.class)))
                .thenAnswer(invocation -> {
                    Supplier<Void> supplier = invocation.getArgument(0);
                    return supplier.get();
                });
    }

    @Test
    @DisplayName("Should set mode to OK")
    void testSetModeOk() throws Exception {
        mockMvc.perform(post("/demo/mode/OK"))
                .andExpect(status().isOk())
                .andExpect(content().string("Demo mode set to: OK"));

        verify(demoSwitch).set(DemoSwitch.Mode.OK);
    }
    
    @Test
    @DisplayName("Should set mode to OK (lowercase)")
    void testSetModeOkLowercase() throws Exception {
        mockMvc.perform(post("/demo/mode/ok"))
                .andExpect(status().isOk())
                .andExpect(content().string("Demo mode set to: OK"));

        verify(demoSwitch).set(DemoSwitch.Mode.OK);
    }

    @Test
    @DisplayName("Should set mode to FAIL")
    void testSetModeFail() throws Exception {
        mockMvc.perform(post("/demo/mode/FAIL"))
                .andExpect(status().isOk())
                .andExpect(content().string("Demo mode set to: FAIL"));

        verify(demoSwitch).set(DemoSwitch.Mode.FAIL);
    }

    @Test
    @DisplayName("Should set mode to SLOW")
    void testSetModeSlow() throws Exception {
        mockMvc.perform(post("/demo/mode/SLOW"))
                .andExpect(status().isOk())
                .andExpect(content().string("Demo mode set to: SLOW"));

        verify(demoSwitch).set(DemoSwitch.Mode.SLOW);
    }
    
    @Test
    @DisplayName("Should return 400 for invalid mode")
    void testSetModeInvalid() throws Exception {
        mockMvc.perform(post("/demo/mode/INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Use one of: OK, FAIL, SLOW"));

        verify(demoSwitch, never()).set(any());
    }
    
    @Test
    @DisplayName("Should trigger indexViaCB and call service")
    void testIndexViaCB() throws Exception {
        Long carId = 123L;

        mockMvc.perform(post("/demo/index/{carId}", carId))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Triggered indexing for carId=123 via CB 'carSearch'"));
        
        verify(cbFactory).create("carSearch");
        verify(circuitBreaker).run(any(Supplier.class), any(Function.class));
        verify(carSearchService).indexCar(carId);
    }
}