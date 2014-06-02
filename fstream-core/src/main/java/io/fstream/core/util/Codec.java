/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.util;

import static lombok.AccessLevel.PRIVATE;

import java.io.InputStream;
import java.io.OutputStream;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

@NoArgsConstructor(access = PRIVATE)
public final class Codec {

  private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JodaModule());

  @SneakyThrows
  public static <T> String encodeText(T value) {
    return MAPPER.writeValueAsString(value);
  }

  @SneakyThrows
  public static <T> void encodeText(OutputStream stream, T value) {
    MAPPER.writeValue(stream, value);
  }

  @SneakyThrows
  public static <T> T decodeText(String text, Class<T> type) {
    try {
      return MAPPER.readValue(text, type);
    } catch (Throwable t) {
      throw new RuntimeException("Error converting '" + text + "' to " + type, t);
    }
  }

  @SneakyThrows
  public static <T> T decodeText(String text, TypeReference<T> type) {
    return MAPPER.readValue(text, type);
  }

  @SneakyThrows
  public static <T> T decodeText(InputStream stream, Class<T> type) {
    return MAPPER.readValue(stream, type);
  }

  @SneakyThrows
  public static <T> byte[] encodeBytes(T value) {
    return MAPPER.writeValueAsBytes(value);
  }

  @SneakyThrows
  public static <T> void encodeBytes(OutputStream stream, T value) {
    MAPPER.writeValue(stream, value);
  }

  @SneakyThrows
  public static <T> T decodeBytes(byte[] bytes, Class<T> type) {
    return MAPPER.readValue(bytes, type);
  }

  @SneakyThrows
  public static <T> T decodeBytes(byte[] bytes, TypeReference<T> type) {
    return MAPPER.readValue(bytes, type);
  }

  @SneakyThrows
  public static <T> T decodeBytes(InputStream stream, Class<T> type) {
    return MAPPER.readValue(stream, type);
  }

}
