/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.bolt;

import lombok.SneakyThrows;
import lombok.val;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class AdapterBolt extends BaseBasicBolt {

  @Override
  @SneakyThrows
  public void execute(Tuple tuple, BasicOutputCollector collector) {
    val value = (String) tuple.getValue(0);

    collector.emit(new Values("1", value));
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields(KafkaBolt.BOLT_KEY, KafkaBolt.BOLT_MESSAGE));
  }

}
