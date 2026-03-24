package com.zgamelogic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Ben Shabowski
 * @since A long time ago
 */
@SpringBootApplication
@EnableScheduling
public class App {
    static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
