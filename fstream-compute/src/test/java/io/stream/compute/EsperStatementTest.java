/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.stream.compute;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.junit.Test;

@Slf4j
public class EsperStatementTest extends AbstractEsperStatementTest {

  @Test
  public void testStatement1() {
    val results = execute(eplFile("statement1.epl"), tickEventFile("tick-events1.json"));
    for (val result : results) {
      log.info("Result: {}", result);
    }
  }

}
