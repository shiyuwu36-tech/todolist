package com.example.todolist.controller;

import com.example.todolist.dto.TaskRequest;
import com.example.todolist.dto.TaskResponse;
import com.example.todolist.model.Task;
import com.example.todolist.service.TaskService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TaskController {
  private final TaskService taskService;

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @GetMapping
  public Map<String, Object> list() {
    List<TaskResponse> items = taskService.list().stream().map(TaskController::toResponse).toList();
    Map<String, Object> resp = new HashMap<>();
    resp.put("items", items);
    return resp;
  }

  @GetMapping("/{id}")
  public TaskResponse get(@PathVariable String id) {
    return toResponse(taskService.get(id));
  }

  @PostMapping
  public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
    Task created = taskService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
  }

  @PutMapping("/{id}")
  public TaskResponse update(@PathVariable String id, @Valid @RequestBody TaskRequest request) {
    return toResponse(taskService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    taskService.delete(id);
    return ResponseEntity.noContent().build();
  }

  private static TaskResponse toResponse(Task task) {
    TaskResponse resp = new TaskResponse();
    resp.setId(task.getId());
    resp.setTitle(task.getTitle());
    resp.setDescription(task.getDescription());
    resp.setStatus(task.getStatus());
    resp.setDueDate(task.getDueDate() != null ? task.getDueDate().toString() : null);
    resp.setCreatedAt(task.getCreatedAt() != null ? task.getCreatedAt().toString() : null);
    resp.setUpdatedAt(task.getUpdatedAt() != null ? task.getUpdatedAt().toString() : null);
    resp.setAttachmentCount(task.getAttachmentCount());
    return resp;
  }
}
