/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.stream.compute;

import org.junit.Test;

public class EsperStatementTest extends AbstractEsperStatementTest {

  @Test
  public void testStatement1File() {
    execute(
        eplFile("statement1.epl"),
        tickEventFile("tick-events1.json"));
  }

  @Test
  public void testStatement1Java() {
    execute(
        epl("SELECT ask FROM Rate"),
        givenEvents(
            tickEvent(1, "EUR/USD", 1.4, 1.3),
            timeEvent(2),
            tickEvent(3, "EUR/USD", 1.41, 1.31),
            timeEvent(4)
        ));
  }
}
