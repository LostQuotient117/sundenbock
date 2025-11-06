package de.nak.iaa.sundenbock;

import de.nak.iaa.sundenbock.config.properties.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class SundenbockApplication {
    public static void main(String[] args) {
        SpringApplication.run(SundenbockApplication.class, args);
    }
}