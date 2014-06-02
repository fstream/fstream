/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.test.kafka;

import static lombok.AccessLevel.PRIVATE;

import java.util.Properties;

import kafka.admin.AdminUtils;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;

import org.I0Itec.zkclient.ZkClient;

/**
 * @see http://stackoverflow.com/a/23360100/527333
 */
@NoArgsConstructor(access = PRIVATE)
public final class KafkaUtils {

  public static void createTopic(@NonNull ZkClient zkClient, @NonNull String topicName) {
    val numPartitions = 1;
    val replicationFactor = 1;
    val topicConfig = new Properties();

    AdminUtils.createTopic(zkClient, topicName, numPartitions, replicationFactor, topicConfig);
  }

}
