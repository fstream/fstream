/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.test.spark;

import org.apache.spark.sql.hive.thriftserver.HiveThriftServer2;

public class EmbeddedSparkServer {

  public static void main(String[] args) {
    HiveThriftServer2
        .main(new String[] {
            "--hiveconf", "spark.master=local",
            "--hiveconf", "javax.jdo.option.ConnectionURL=jdbc:derby:;databaseName=build/metastore_db;create=true",
            "--hiveconf", "derby.stream.error.file=build/derby.log"
        });
  }

}
