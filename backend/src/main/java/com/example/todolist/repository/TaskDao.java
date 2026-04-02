package com.example.todolist.repository;

import com.example.todolist.model.Task;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TaskDao {
  private final JdbcTemplate jdbcTemplate;

  public TaskDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<Task> findAll() {
    String sql = "SELECT t.*, (SELECT COUNT(1) FROM attachments a WHERE a.task_id = t.id) AS attachment_count FROM tasks t ORDER BY created_at DESC";
    return jdbcTemplate.query(sql, new TaskMapper());
  }

  public List<Task> findAll(String from, String to) {
    StringBuilder sql = new StringBuilder(
      "SELECT t.*, (SELECT COUNT(1) FROM attachments a WHERE a.task_id = t.id) AS attachment_count FROM tasks t"
    );
    List<Object> params = new ArrayList<>();

    if (from != null && !from.isBlank()) {
      sql.append(" WHERE t.due_date IS NOT NULL AND t.due_date >= ?");
      params.add(from);
    }

    if (to != null && !to.isBlank()) {
      sql.append(params.isEmpty() ? " WHERE" : " AND");
      sql.append(" t.due_date IS NOT NULL AND t.due_date <= ?");
      params.add(to);
    }

    sql.append(" ORDER BY created_at DESC");
    return params.isEmpty()
      ? findAll()
      : jdbcTemplate.query(sql.toString(), new TaskMapper(), params.toArray());
  }

  public Optional<Task> findById(String id) {
    String sql = "SELECT t.*, (SELECT COUNT(1) FROM attachments a WHERE a.task_id = t.id) AS attachment_count FROM tasks t WHERE t.id = ?";
    List<Task> list = jdbcTemplate.query(sql, new TaskMapper(), id);
    return list.stream().findFirst();
  }

  public void insert(Task task) {
    String sql = "INSERT INTO tasks (id, title, description, status, due_date, created_at, updated_at) VALUES (?,?,?,?,?,?,?)";
    jdbcTemplate.update(
      sql,
      task.getId(),
      task.getTitle(),
      task.getDescription(),
      task.getStatus(),
      toString(task.getDueDate()),
      toString(task.getCreatedAt()),
      toString(task.getUpdatedAt())
    );
  }

  public void update(Task task) {
    String sql = "UPDATE tasks SET title=?, description=?, status=?, due_date=?, updated_at=? WHERE id=?";
    jdbcTemplate.update(
      sql,
      task.getTitle(),
      task.getDescription(),
      task.getStatus(),
      toString(task.getDueDate()),
      toString(task.getUpdatedAt()),
      task.getId()
    );
  }

  public int delete(String id) {
    return jdbcTemplate.update("DELETE FROM tasks WHERE id=?", id);
  }

  private static String toString(OffsetDateTime time) {
    return time != null ? time.toString() : null;
  }

  private static class TaskMapper implements RowMapper<Task> {
    @Override
    public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
      Task task = new Task();
      task.setId(rs.getString("id"));
      task.setTitle(rs.getString("title"));
      task.setDescription(rs.getString("description"));
      task.setStatus(rs.getString("status"));
      String due = rs.getString("due_date");
      task.setDueDate(due != null ? OffsetDateTime.parse(due) : null);
      task.setCreatedAt(OffsetDateTime.parse(rs.getString("created_at")));
      task.setUpdatedAt(OffsetDateTime.parse(rs.getString("updated_at")));
      task.setAttachmentCount(rs.getLong("attachment_count"));
      return task;
    }
  }
}
