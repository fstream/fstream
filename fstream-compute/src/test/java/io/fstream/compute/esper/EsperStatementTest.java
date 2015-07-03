/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.esper;

import org.junit.Test;

public class EsperStatementTest extends AbstractEsperStatementTest {

  @Test
  public void testStatement1File() {
    execute(
        eplFile("statement1.epl"),
        givenEvents(
            quoteEventFile("quote-events1.json")));
  }

  @Test
  public void testStatement1Java() {
    execute(
        epl("SELECT ask FROM Rate"),
        givenEvents(
            quoteEvent(second("00:00:00"), "EUR/USD", 1.40, 1.30),
            timeEvent(second("00:00:01")),
            quoteEvent(second("00:00:02"), "EUR/USD", 1.41, 1.31),
            timeEvent(second("00:00:03"))));
  }

}
