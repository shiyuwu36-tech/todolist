package com.example.todolist.service;

import com.example.todolist.dto.TaskRequest;
import com.example.todolist.exception.NotFoundException;
import com.example.todolist.exception.ValidationException;
import com.example.todolist.model.Task;
import com.example.todolist.repository.AttachmentDao;
import com.example.todolist.repository.TaskDao;
import com.example.todolist.service.FileStorageService;
import com.example.todolist.util.DateUtil;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TaskService {
  private static final Set<String> ALLOWED_STATUS = Set.of("todo", "doing", "done");
  private final TaskDao taskDao;
  private final AttachmentDao attachmentDao;
  private final FileStorageService fileStorageService;

  public TaskService(TaskDao taskDao, AttachmentDao attachmentDao, FileStorageService fileStorageService) {
    this.taskDao = taskDao;
    this.attachmentDao = attachmentDao;
    this.fileStorageService = fileStorageService;
  }

  public List<Task> list(String from, String to) {
    return taskDao.findAll(from, to);
  }

  public Task get(String id) {
    return taskDao.findById(id).orElseThrow(() -> new NotFoundException("任务不存在"));
  }

  public Task create(TaskRequest request) {
    validateStatus(request.getStatus());
    OffsetDateTime now = DateUtil.now();
    Task task = new Task();
    task.setId(UUID.randomUUID().toString());
    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setStatus(defaultStatus(request.getStatus()));
    task.setDueDate(parseDueDate(request.getDueDate()));
    task.setCreatedAt(now);
    task.setUpdatedAt(now);
    taskDao.insert(task);
    task.setAttachmentCount(0);
    return task;
  }

  public Task update(String id, TaskRequest request) {
    Task existing = get(id);
    validateStatus(request.getStatus());
    existing.setTitle(request.getTitle());
    existing.setDescription(request.getDescription());
    existing.setStatus(defaultStatus(request.getStatus()));
    existing.setDueDate(parseDueDate(request.getDueDate()));
    existing.setUpdatedAt(DateUtil.now());
    taskDao.update(existing);
    return get(id);
  }

  public void delete(String id) {
    // ensure exist
    get(id);
    attachmentDao.findByTaskId(id).forEach(att -> fileStorageService.delete(fileStorageService.load(att.getStoredName())));
    attachmentDao.deleteByTaskId(id);
    taskDao.delete(id);
  }

  private static void validateStatus(String status) {
    if (!StringUtils.hasText(status)) {
      return;
    }
    if (!ALLOWED_STATUS.contains(status)) {
      throw new ValidationException("非法状态", status);
    }
  }

  private static String defaultStatus(String status) {
    return StringUtils.hasText(status) ? status : "todo";
  }

  private static OffsetDateTime parseDueDate(String value) {
    try {
      return DateUtil.parse(value);
    } catch (Exception ex) {
      throw new ValidationException("截止时间格式错误", value);
    }
  }
}
