package com.myblogbackend.blog.config;

import com.myblogbackend.blog.exception.FeignClientDecoder;
import com.myblogbackend.blog.services.PostElasticsService;
import feign.codec.ErrorDecoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfigs {
  @Bean
  public ErrorDecoder myErrorDecoder() {
    return new FeignClientDecoder();
  }
  @Bean
  public CommandLineRunner initElasticsearch(PostElasticsService postElasticsService) {
    return args -> postElasticsService.syncDatabaseToPostElastics();
  }
}
