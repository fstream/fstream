package io.fstream.test.kafka;

import java.io.IOException;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@Slf4j
public class EmbeddedZooKeeperServerTest {

  public @Rule
  TemporaryFolder tmp = new TemporaryFolder();

  @Test
  public void testServer() throws IOException {
    log.info("Starting embedded ZooKeeper server");
    val zk = new EmbeddedZooKeeperServer(tmp.newFolder());
    zk.startAsync();
    zk.awaitRunning();

    log.info("Starting embedded Kafka server");
    val server = new EmbeddedKafkaServer();
    server.startAsync();
    server.awaitRunning();

    System.out.println("\n\n*** Running embedded Kafka server. Press any key to shutdown\n\n");
    System.in.read();

    server.stopAsync();
    server.awaitTerminated();

    zk.stopAsync();
    zk.awaitTerminated();
  }

}
