package com.example.todolist.service;

import com.example.todolist.exception.NotFoundException;
import com.example.todolist.model.Attachment;
import com.example.todolist.repository.AttachmentDao;
import com.example.todolist.repository.TaskDao;
import com.example.todolist.util.DateUtil;
import com.example.todolist.util.FileTypeUtil;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentService {
  private final AttachmentDao attachmentDao;
  private final TaskDao taskDao;
  private final FileStorageService fileStorageService;

  public AttachmentService(AttachmentDao attachmentDao, TaskDao taskDao, FileStorageService fileStorageService) {
    this.attachmentDao = attachmentDao;
    this.taskDao = taskDao;
    this.fileStorageService = fileStorageService;
  }

  public List<Attachment> listByTask(String taskId) {
    ensureTaskExists(taskId);
    return attachmentDao.findByTaskId(taskId);
  }

  public Attachment get(String id) {
    return attachmentDao.findById(id).orElseThrow(() -> new NotFoundException("附件不存在"));
  }

  public Attachment upload(String taskId, MultipartFile file) {
    ensureTaskExists(taskId);
    FileTypeUtil.ensureAllowed(file.getContentType());
    FileStorageService.StoredFile stored = fileStorageService.save(file);
    OffsetDateTime now = DateUtil.now();
    Attachment attachment = new Attachment();
    attachment.setId(UUID.randomUUID().toString());
    attachment.setTaskId(taskId);
    attachment.setOriginalName(file.getOriginalFilename());
    attachment.setStoredName(stored.storedName());
    attachment.setMimeType(file.getContentType());
    attachment.setSize(file.getSize());
    attachment.setStoredPath(stored.relativePath());
    attachment.setCreatedAt(now);
    attachmentDao.insert(attachment);
    return attachment;
  }

  public Resource loadAsResource(Attachment attachment) {
    Path path = fileStorageService.load(attachment.getStoredName());
    return new FileSystemResource(path);
  }

  private void ensureTaskExists(String taskId) {
    taskDao.findById(taskId).orElseThrow(() -> new NotFoundException("任务不存在"));
  }
}
