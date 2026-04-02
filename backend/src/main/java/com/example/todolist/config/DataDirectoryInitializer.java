package com.example.todolist.config;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DataDirectoryInitializer {
  private static final Logger log = LoggerFactory.getLogger(DataDirectoryInitializer.class);

  @PostConstruct
  public void init() {
    try {
      Files.createDirectories(Path.of("data"));
    } catch (IOException e) {
      log.error("创建数据目录失败", e);
      throw new RuntimeException(e);
    }
  }
}
