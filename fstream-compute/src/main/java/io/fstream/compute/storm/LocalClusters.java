/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.storm;

import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import backtype.storm.Config;
import backtype.storm.ILocalCluster;
import backtype.storm.LocalCluster;
import backtype.storm.Testing;
import backtype.storm.testing.MkClusterParam;
import backtype.storm.testing.TestJob;

@NoArgsConstructor(access = PRIVATE)
public final class LocalClusters {

  @NonNull
  public static LocalCluster createLocalCluster(List<String> zkServers, long zkPort) {
    val daemonConf = new Config();
    daemonConf.put(Config.TOPOLOGY_ACKER_EXECUTORS, 0);
    daemonConf.put(Config.STORM_ZOOKEEPER_SERVERS, zkServers);
    daemonConf.put(Config.STORM_ZOOKEEPER_PORT, zkPort);

    val clusterParams = new MkClusterParam();
    clusterParams.setSupervisors(5);
    clusterParams.setPortsPerSupervisor(5);
    clusterParams.setDaemonConf(daemonConf);

    return createLocalCluster(clusterParams);
  }

  /**
   * Hack to override local cluster workers.
   * 
   * @see http
   * ://grokbase.com/t/gg/storm-user/132m7ydzzb/localcluster-topology-not-working-is-there-a-max-number-of-topologies
   * -to-deploy
   */
  @SneakyThrows
  private static LocalCluster createLocalCluster(final MkClusterParam clusterParams) {
    val reference = new AtomicReference<LocalCluster>();
    val latch = new CountDownLatch(1);
    val thread = new Thread(new Runnable() {

      @Override
      public void run() {
        Testing.withLocalCluster(clusterParams,
            new TestJob() {

              @Override
              @SneakyThrows
              public void run(final ILocalCluster cluster) {
                reference.set((LocalCluster) cluster);
                latch.countDown();

                // Wait forever
                synchronized (this) {
                  while (true) {
                    this.wait();
                  }
                }
              }

            });

      }

    });

    thread.setDaemon(true);
    thread.start();

    latch.await();
    return reference.get();
  }

}
