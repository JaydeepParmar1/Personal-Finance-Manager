package com.personalfinance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point of the Personal Finance Manager application.
 *
 * <p>Boots the Spring Boot web application on port 8080 (see
 * {@code application.yml}).</p>
 */
@SpringBootApplication
public class PersonalFinanceApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PersonalFinanceApplication.class, args);
    }
}
