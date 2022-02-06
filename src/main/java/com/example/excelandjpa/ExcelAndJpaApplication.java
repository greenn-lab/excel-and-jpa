package com.example.excelandjpa;

import java.util.concurrent.Executor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableAsync
public class ExcelAndJpaApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExcelAndJpaApplication.class, args);
  }

  @Bean
  Executor asyncGenerateExcelExecutor() {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(16);
    executor.setQueueCapacity(128);
    executor.setThreadNamePrefix("gen-excel-");

    return executor;
  }

  @Bean
  Executor defaultExecutor() {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(16);
    executor.setThreadNamePrefix("default-exec-");

    return executor;
  }

}
