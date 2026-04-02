package com.example.todolist.service;

import com.example.todolist.exception.PayloadTooLargeException;
import com.example.todolist.exception.StorageException;
import com.example.todolist.util.FilePathUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
  private final Path root;

  public FileStorageService(@Value("${app.storage.upload-dir}") String uploadDir) {
    this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(this.root);
    } catch (IOException e) {
      throw new StorageException("创建上传目录失败", e.getMessage());
    }
  }

  public StoredFile save(MultipartFile file) {
    if (file.getSize() > 10 * 1024 * 1024) {
      throw new PayloadTooLargeException("文件超过 10MB 限制");
    }
    String ext = getExtension(file.getOriginalFilename());
    String storedName = UUID.randomUUID().toString() + ext;
    Path target = FilePathUtil.resolveSafe(root, storedName);
    try {
      Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new StorageException("保存文件失败", e.getMessage());
    }
    String relativePath = root.getFileName().resolve(storedName).toString();
    return new StoredFile(storedName, relativePath, target);
  }

  public Path load(String storedName) {
    return FilePathUtil.resolveSafe(root, storedName);
  }

  public void delete(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException e) {
      throw new StorageException("删除文件失败", e.getMessage());
    }
  }

  private static String getExtension(String filename) {
    if (!StringUtils.hasText(filename)) {
      return "";
    }
    int idx = filename.lastIndexOf('.');
    if (idx == -1) {
      return "";
    }
    return filename.substring(idx);
  }

  public record StoredFile(String storedName, String relativePath, Path absolutePath) {}
}
