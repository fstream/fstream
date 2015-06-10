package io.fstream.simulate.publisher;

import io.fstream.core.util.Codec;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.PreDestroy;

import lombok.SneakyThrows;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("file")
public class FilePublisher implements Publisher, Closeable {

  private final PrintWriter writer;

  @SneakyThrows
  public FilePublisher() {
    this.writer = new PrintWriter(new File("/tmp/fstream-simulate.json"));
  }

  @Override
  @SneakyThrows
  public void publish(Object message) {
    writer.println(Codec.encodeText(message));
  }

  @Override
  @PreDestroy
  public void close() throws IOException {
    writer.close();
  }

}
