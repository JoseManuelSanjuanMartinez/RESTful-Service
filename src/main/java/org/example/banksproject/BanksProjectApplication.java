package org.example.banksproject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BanksProjectApplication {
    private static final Logger logger = LoggerFactory.getLogger(BanksProjectApplication.class);

    public static void main(String[] args) {
        logger.warn("Initiating Banking Service...");
        SpringApplication.run(BanksProjectApplication.class, args);
    }

}
