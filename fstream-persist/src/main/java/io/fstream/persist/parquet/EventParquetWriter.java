/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.parquet;

import io.fstream.core.model.event.Event;

import java.io.Closeable;

public interface EventParquetWriter extends Closeable {

  void write(Event event);

}
