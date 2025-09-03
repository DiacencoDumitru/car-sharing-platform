package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class PidController {
    @GetMapping("/pid")
    public Map<String, Object> pid() {
        return Map.of("pid", ProcessHandle.current().pid());
    }
}