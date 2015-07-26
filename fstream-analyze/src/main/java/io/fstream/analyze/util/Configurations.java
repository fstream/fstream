package io.fstream.analyze.util;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.Map;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapred.JobConf;

@UtilityClass
public class Configurations {

  @NonNull
  public static void addCompressionCodec(JobConf conf, Class<? extends CompressionCodec> codecClass) {
    val codecsProperty = "io.compression.codecs";
    val currentCodecs = conf.get(codecsProperty);
    val codecs = codecClass.getName() + (isNullOrEmpty(currentCodecs) ? "" : "," + currentCodecs);

    conf.set(codecsProperty, codecs);
  }

  @NonNull
  public static void setAll(Configuration conf, Map<String, String> properties) {
    for (val entry : properties.entrySet()) {
      conf.set(entry.getKey(), entry.getValue());
    }
  }

}
