package com.myblogbackend.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@ConfigurationPropertiesScan(basePackages = {"com.myblogbackend.blog"})
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableTransactionManagement
public class BlogApplication {

    public static void main(final String[] args) {
        SpringApplication.run(BlogApplication.class, args);
//        System.out.println("STARTED SPRING BOOT APPLICATION");
    }

}
