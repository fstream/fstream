/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.util;

import io.fstream.core.model.event.TickEvent;
import io.fstream.core.util.Codec;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;

public class CodecDataFormat implements DataFormat {

  @Override
  public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
    Codec.encode(stream, graph);
  }

  @Override
  public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
    return Codec.decode(stream, TickEvent.class);
  }

}
