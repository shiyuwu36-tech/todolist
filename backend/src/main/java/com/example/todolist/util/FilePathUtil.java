package com.example.todolist.util;

import com.example.todolist.exception.StorageException;
import java.nio.file.Path;

public final class FilePathUtil {
  private FilePathUtil() {}

  public static Path resolveSafe(Path base, String filename) {
    Path resolved = base.resolve(filename).normalize();
    if (!resolved.startsWith(base)) {
      throw new StorageException("非法路径", filename);
    }
    return resolved;
  }
}
