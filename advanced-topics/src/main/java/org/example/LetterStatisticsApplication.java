package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LetterStatisticsApplication {

    public static void main(String[] args) {
        var ctx = SpringApplication.run(LetterStatisticsApplication.class, args);
        long pid = ProcessHandle.current().pid();
        System.out.println("Running JAR: advanced-topics | PID=" + pid + " | Spring Boot started");
    }
}
