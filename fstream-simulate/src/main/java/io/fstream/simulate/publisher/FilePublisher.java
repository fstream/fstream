package io.fstream.simulate.publisher;

import io.fstream.core.util.Codec;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class FilePublisher implements Publisher, Closeable {

  private final PrintWriter writer;

  @SneakyThrows
  public FilePublisher(@NonNull File file) {
    this.writer = new PrintWriter(file);
  }

  @Override
  @SneakyThrows
  public void publish(Object message) {
    writer.println(Codec.encodeText(message));
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }

}
