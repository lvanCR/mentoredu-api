package com.mentoredu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.mentoredu")
public class MentoreduApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MentoreduApiApplication.class, args);
    }

}
