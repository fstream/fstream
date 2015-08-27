package io.fstream.compute.esper;

import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.EventType;
import io.fstream.core.util.Codec;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.PreDestroy;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("file")
public class EsperFileProducer implements EsperProducer, Closeable {

  private final PrintWriter alertWriter;
  private final PrintWriter metricWriter;

  @SneakyThrows
  @Autowired
  public EsperFileProducer(@Value("${compute.file.dir}") File outputDir) {
    log.info("Creating writers to {} ...", outputDir);
    this.alertWriter = new PrintWriter(new File(outputDir, "fstream-simulate-alerts.json"));
    this.metricWriter = new PrintWriter(new File(outputDir, "fstream-simulate-metrics.json"));
  }

  @Override
  @SneakyThrows
  public void send(Event event) {
    val writer = getWriter(event);
    writer.println(Codec.encodeText(event));
  }

  private PrintWriter getWriter(Event event) {
    if (event.getType() == EventType.ALERT) {
      return alertWriter;
    } else if (event.getType() == EventType.METRIC) {
      return metricWriter;
    }

    throw new IllegalStateException();
  }

  @Override
  @PreDestroy
  public void close() throws IOException {
    alertWriter.close();
    metricWriter.close();
  }

}
