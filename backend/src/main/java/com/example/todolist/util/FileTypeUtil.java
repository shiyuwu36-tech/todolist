package com.example.todolist.util;

import com.example.todolist.exception.UnsupportedMediaTypeException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class FileTypeUtil {
  private static final Set<String> ALLOWED = new HashSet<>(Arrays.asList(
    "application/pdf",
    "text/plain"
  ));

  private FileTypeUtil() {}

  public static void ensureAllowed(String mimeType) {
    if (mimeType == null) {
      throw new UnsupportedMediaTypeException("文件类型缺失");
    }
    String normalized = mimeType.toLowerCase(Locale.ROOT);
    if (normalized.startsWith("image/")) {
      return;
    }
    if (ALLOWED.contains(normalized)) {
      return;
    }
    throw new UnsupportedMediaTypeException("不支持的文件类型");
  }
}
