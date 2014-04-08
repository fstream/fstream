package io.fstream.test.kafka;

import java.io.IOException;

import lombok.val;

import org.junit.Test;

public class EmbeddedZooKeeperServerTest {

  @Test
  public void testServer() throws IOException {
    val zk = new EmbeddedZooKeeperServer();
    zk.startAsync();
    zk.awaitRunning();

    val server = new EmbeddedKafkaServer();
    server.startAsync();
    server.awaitRunning();

    System.in.read();
  }

}
