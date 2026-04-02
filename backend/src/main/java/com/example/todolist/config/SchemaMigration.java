package com.example.todolist.config;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaMigration {
  private static final Logger log = LoggerFactory.getLogger(SchemaMigration.class);
  private final JdbcTemplate jdbcTemplate;

  public SchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @PostConstruct
  public void migrate() {
    ensureTasksPriorityColumn();
  }

  private void ensureTasksPriorityColumn() {
    List<Map<String, Object>> columns = jdbcTemplate.queryForList("PRAGMA table_info(tasks)");
    boolean hasPriority = columns.stream().anyMatch(col -> "priority".equalsIgnoreCase((String) col.get("name")));
    if (hasPriority) {
      return;
    }
    log.info("Adding priority column to tasks table");
    jdbcTemplate.execute("ALTER TABLE tasks ADD COLUMN priority TEXT NOT NULL DEFAULT 'medium'");
    jdbcTemplate.execute("UPDATE tasks SET priority='medium' WHERE priority IS NULL OR priority='' ");
  }
}
