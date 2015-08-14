package io.fstream.simulate.output;

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
public class FileOutput implements Output, Closeable {

  private final PrintWriter writer;

  @SneakyThrows
  public FileOutput() {
    this.writer = new PrintWriter(new File("/tmp/fstream-simulate.json"));
  }

  @Override
  @SneakyThrows
  public void write(Object message) {
    writer.println(Codec.encodeText(message));
  }

  @Override
  @PreDestroy
  public void close() throws IOException {
    writer.close();
  }

}
