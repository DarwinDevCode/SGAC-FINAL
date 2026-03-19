package org.uteq.sgacfinal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SgacFinalApplication {

    public static void main(String[] args) {
        SpringApplication.run(SgacFinalApplication.class, args);
    }
}
