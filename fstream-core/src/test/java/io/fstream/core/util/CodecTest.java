/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import io.fstream.core.model.event.AlertEvent;
import io.fstream.core.model.event.Event;
import lombok.val;

import org.joda.time.DateTime;
import org.junit.Test;

public class CodecTest {

  @Test
  public void testRoundTrip() {
    val expected = new AlertEvent(new DateTime(), 1, "data");
    val text = Codec.encodeText(expected);
    System.out.println(text);
    val actual = Codec.decodeText(text, Event.class);

    assertThat(actual).isEqualTo(expected);
  }

}
