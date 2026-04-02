package com.example.todolist.controller;

import com.example.todolist.dto.AttachmentResponse;
import com.example.todolist.model.Attachment;
import com.example.todolist.service.AttachmentService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
public class AttachmentController {
  private final AttachmentService attachmentService;

  public AttachmentController(AttachmentService attachmentService) {
    this.attachmentService = attachmentService;
  }

  @GetMapping("/tasks/{taskId}/attachments")
  public Map<String, Object> list(@PathVariable String taskId) {
    List<AttachmentResponse> items = attachmentService.listByTask(taskId).stream().map(AttachmentController::toResponse).toList();
    Map<String, Object> resp = new HashMap<>();
    resp.put("items", items);
    return resp;
  }

  @PostMapping("/tasks/{taskId}/attachments")
  public ResponseEntity<AttachmentResponse> upload(@PathVariable String taskId, @RequestParam("file") MultipartFile file) {
    Attachment created = attachmentService.upload(taskId, file);
    return ResponseEntity.status(201).body(toResponse(created));
  }

  @GetMapping("/attachments/{id}")
  public ResponseEntity<Resource> download(@PathVariable String id) {
    Attachment attachment = attachmentService.get(id);
    Resource resource = attachmentService.loadAsResource(attachment);
    String encoded = URLEncoder.encode(attachment.getOriginalName(), StandardCharsets.UTF_8);
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encoded + "\"")
      .contentType(MediaType.parseMediaType(attachment.getMimeType()))
      .body(resource);
  }

  private static AttachmentResponse toResponse(Attachment attachment) {
    AttachmentResponse resp = new AttachmentResponse();
    resp.setId(attachment.getId());
    resp.setOriginalName(attachment.getOriginalName());
    resp.setMimeType(attachment.getMimeType());
    resp.setSize(attachment.getSize());
    resp.setCreatedAt(attachment.getCreatedAt() != null ? attachment.getCreatedAt().toString() : null);
    return resp;
  }
}
