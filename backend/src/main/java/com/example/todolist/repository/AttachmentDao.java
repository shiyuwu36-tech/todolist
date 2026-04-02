package com.example.todolist.repository;

import com.example.todolist.model.Attachment;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AttachmentDao {
  private final JdbcTemplate jdbcTemplate;

  public AttachmentDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void insert(Attachment attachment) {
    String sql = "INSERT INTO attachments (id, task_id, original_name, stored_name, mime_type, size, stored_path, created_at) VALUES (?,?,?,?,?,?,?,?)";
    jdbcTemplate.update(
      sql,
      attachment.getId(),
      attachment.getTaskId(),
      attachment.getOriginalName(),
      attachment.getStoredName(),
      attachment.getMimeType(),
      attachment.getSize(),
      attachment.getStoredPath(),
      toString(attachment.getCreatedAt())
    );
  }

  public List<Attachment> findByTaskId(String taskId) {
    String sql = "SELECT * FROM attachments WHERE task_id = ? ORDER BY created_at DESC";
    return jdbcTemplate.query(sql, new AttachmentMapper(), taskId);
  }

  public Optional<Attachment> findById(String id) {
    String sql = "SELECT * FROM attachments WHERE id = ?";
    List<Attachment> list = jdbcTemplate.query(sql, new AttachmentMapper(), id);
    return list.stream().findFirst();
  }

  public void deleteByTaskId(String taskId) {
    jdbcTemplate.update("DELETE FROM attachments WHERE task_id = ?", taskId);
  }

  private static String toString(OffsetDateTime time) {
    return time != null ? time.toString() : null;
  }

  private static class AttachmentMapper implements RowMapper<Attachment> {
    @Override
    public Attachment mapRow(ResultSet rs, int rowNum) throws SQLException {
      Attachment a = new Attachment();
      a.setId(rs.getString("id"));
      a.setTaskId(rs.getString("task_id"));
      a.setOriginalName(rs.getString("original_name"));
      a.setStoredName(rs.getString("stored_name"));
      a.setMimeType(rs.getString("mime_type"));
      a.setSize(rs.getLong("size"));
      a.setStoredPath(rs.getString("stored_path"));
      a.setCreatedAt(OffsetDateTime.parse(rs.getString("created_at")));
      return a;
    }
  }
}
