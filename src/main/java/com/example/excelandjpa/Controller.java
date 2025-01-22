package com.example.excelandjpa;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class Controller {

  private final Service service;


  @GetMapping("excel")
  public SseEmitter excel(HttpServletResponse response) {

    final SseEmitter emitter = new SseEmitter();
    emitter.onError(t -> log.info("SSE ERROR!!"));

    service.excel(emitter);

    log.info("EMITTED!!");
    return emitter;
  }

}
