/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.readLines;
import static io.fstream.compute.factory.StormFactory.newConfig;
import static io.fstream.compute.factory.StormFactory.newStormTopology;
import static java.lang.System.in;
import static java.lang.System.out;
import static joptsimple.internal.Strings.repeat;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.thrift7.TException;

import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.KillOptions;
import backtype.storm.generated.NotAliveException;
import backtype.storm.utils.NimbusClient;
import backtype.storm.utils.Utils;

@Slf4j
public class Main {

  public static void main(String... args) throws Exception {
    log.info("{}", repeat('-', 100));
    for (val line : readLines(getResource("banner.txt"), UTF_8)) {
      log.info(line);
    }
    log.info("{}", repeat('-', 100));

    new Main().run(args);
  }

  public void run(String... args) throws Exception {
    // Parse args
    val local = args == null || args.length == 0;
    val name = local ? "kafka" : args[0];

    // Setup
    val topology = newStormTopology();
    val config = newConfig(local);

    if (local) {
      val cluster = new LocalCluster();
      cluster.submitTopology(name, config, topology);

      out.println("\n\n*** Running [local] compute. Press any key to shutdown\n\n");
      in.read();

      cluster.shutdown();
    } else {
      StormSubmitter.submitTopology(name, config, topology);

      out.println("\n\n*** Running [cluster] compute. Press any key to shutdown\n\n");
      in.read();

      killTopology(name);
    }
  }

  private static void killTopology(String name) throws NotAliveException, TException {
    val client = NimbusClient.getConfiguredClient(Utils.readStormConfig()).getClient();
    val killOpts = new KillOptions();
    client.killTopologyWithOpts(name, killOpts);
  }

}
