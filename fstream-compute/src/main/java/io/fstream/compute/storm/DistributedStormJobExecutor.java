/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.storm;

import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.thrift7.TException;

import backtype.storm.StormSubmitter;
import backtype.storm.generated.KillOptions;
import backtype.storm.generated.NotAliveException;
import backtype.storm.utils.NimbusClient;
import backtype.storm.utils.Utils;

/**
 * {@code StormJobExecutor} implementation responsible for executing distributed topologies via {@link StormJob}s.
 */
@Slf4j
@Setter
public class DistributedStormJobExecutor extends AbstractStormJobExecutor {

  /**
   * Constants.
   */
  private static final String STORM_JAR_PROPERTY_NAME = "storm.jar";

  @Override
  @SneakyThrows
  public void execute(@NonNull final StormJob job) {
    setStormJar();

    log.info("Submitting cluster topology '{}'...", job.getId());
    StormSubmitter.submitTopology(job.getId(), job.getConfig(), job.getTopology());

    onShutdown(new Runnable() {

      @Override
      @SneakyThrows
      public void run() {
        log.info("Killing topology...");
        killTopology(job.getId());
        log.info("Killed topology");
      }

    });
  }

  @SneakyThrows
  private void setStormJar() {
    // This needs to be changed in to append {@code .getClassLoader().getClass()} if using nested jars (currently not
    // working).
    val anchor = DistributedStormJobExecutor.class;
    val stormJar = anchor.getProtectionDomain().getCodeSource().getLocation().getPath();

    // See http://stackoverflow.com/questions/15781176/how-to-submit-a-topology-in-storm-production-cluster-using-ide
    log.info("Setting {} to {}...", STORM_JAR_PROPERTY_NAME, stormJar);
    System.setProperty(STORM_JAR_PROPERTY_NAME, stormJar);
  }

  private static void killTopology(String name) throws NotAliveException, TException {
    val client = NimbusClient.getConfiguredClient(Utils.readStormConfig()).getClient();
    val killOpts = new KillOptions();

    log.info("Killing topology '{}'", name);
    client.killTopologyWithOpts(name, killOpts);
    log.info("Killed topology '{}'.", name);
  }

}
