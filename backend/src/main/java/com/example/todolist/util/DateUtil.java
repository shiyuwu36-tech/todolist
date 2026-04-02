package com.example.todolist.util;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public final class DateUtil {
  private DateUtil() {}

  public static OffsetDateTime now() {
    return OffsetDateTime.now();
  }

  public static String format(OffsetDateTime time) {
    return Optional.ofNullable(time).map(t -> t.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)).orElse(null);
  }

  public static OffsetDateTime parse(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return OffsetDateTime.parse(value);
  }
}
