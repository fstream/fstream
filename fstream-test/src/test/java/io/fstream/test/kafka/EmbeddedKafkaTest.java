package io.fstream.test.kafka;

import static com.google.common.collect.ImmutableMap.of;
import static java.lang.System.in;
import static java.lang.System.out;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;

import kafka.admin.AdminUtils;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.I0Itec.zkclient.ZkClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@Slf4j
public class EmbeddedKafkaTest {

  public @Rule
  TemporaryFolder tmp = new TemporaryFolder();

  EmbeddedZooKeeper zooKeeper;
  EmbeddedKafka kafka;

  @Before
  public void setUp() throws IOException {
    log.info("> Starting embedded ZooKeeper...");
    zooKeeper = new EmbeddedZooKeeper(tmp.newFolder(), tmp.newFolder());
    zooKeeper.startAsync();
    zooKeeper.awaitRunning();
    log.info("< Started embedded ZooKeeper");

    log.info("> Starting embedded Kafka...");
    kafka = new EmbeddedKafka();
    kafka.startAsync();
    kafka.awaitRunning();
    log.info("< Started embedded Kafka");
  }

  @After
  public void tearDown() throws IOException {
    log.info("> Stopping embedded Kafka...");
    kafka.stopAsync();
    kafka.awaitTerminated();
    log.info("< Stopped embedded Kafka");

    log.info("Stopping embedded ZooKeeper...");
    zooKeeper.stopAsync();
    zooKeeper.awaitTerminated();
    log.info("Stopped embedded ZooKeeper");
  }

  @Test
  public void testServer() throws IOException {
    // createTopic();

    registerConsumer();
    out.println("\n\n*** Running embedded Kafka. Press any key to shutdown\n\n");
    in.read();
  }

  private void registerConsumer() {
    val props = new Properties();
    props.put("zookeeper.connect", "localhost:21818");
    props.put("zookeeper.connection.timeout.ms", "1000000");
    props.put("group.id", "1");
    props.put("broker.id", "0");

    val consumerConnector = Consumer.createJavaConsumerConnector(new ConsumerConfig(props));

    val count = 1;
    val topicMessageStreams = consumerConnector.createMessageStreams(of("test", count));
    val streams = topicMessageStreams.get("test");
    val executor = Executors.newFixedThreadPool(count);

    for (val stream : streams) {
      executor.submit(new Runnable() {

        @Override
        public void run() {
          for (val event : stream) {
            log.info("Received message: {}", new String(event.message()));
          }
        }

      });
    }
  }

  @Ignore
  private void createTopic() {
    val zkClient = new ZkClient("localhost:21818");
    Properties props = new Properties();
    String topic = "test";
    int partitions = 1;
    int replicationFactor = 1;
    AdminUtils.createTopic(zkClient, topic, partitions, replicationFactor, props);
    zkClient.close();
  }

}
