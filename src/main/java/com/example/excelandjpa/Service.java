package com.example.excelandjpa;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@org.springframework.stereotype.Service
@Slf4j
public class Service {

  @Async("asyncGenerateExcelExecutor")
  public void excel(SseEmitter emitter) {

    log.info("ASYNC START!!");

    IntStream.range(0, 3).forEach(i -> {
      try {
        emitter.send(LocalDateTime.now());
      } catch (IOException e) {
        e.printStackTrace();
        emitter.complete();
      }

      try {
        TimeUnit.SECONDS.sleep(2);
        log.info("{} {} RUNNING!!", Thread.currentThread().getName(), i);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });

    log.info("ASYNC FINISH!!");
    emitter.complete();
  }

}
