/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import lombok.val;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Test;

import parquet.column.ParquetProperties;
import parquet.example.data.Group;
import parquet.example.data.simple.SimpleGroupFactory;
import parquet.hadoop.ParquetReader;
import parquet.hadoop.ParquetWriter;
import parquet.hadoop.example.GroupReadSupport;
import parquet.hadoop.example.GroupWriteSupport;
import parquet.schema.MessageType;
import parquet.schema.MessageTypeParser;

public class ParquetWriterTest {

  FileSystem fileSystem;
  Configuration conf = new Configuration();
  Path path = new Path("build/data.parquet");

  @Before
  public void setUp() throws IOException {
    fileSystem = FileSystem.get(conf);
    fileSystem.delete(path, true);
  }

  @Test
  public void testWriter() throws Exception {
    // Write
    try (val writer = createWriter(createSchema())) {
      val groupFactory = new SimpleGroupFactory(createSchema());
      val group = groupFactory.newGroup()
          .append("left", "L")
          .append("right", "R");

      writer.write(group);
    }

    // Read
    val reader = createReader();
    val result = reader.read();

    // Verify
    assertThat(result).isNotNull();
    assertThat(result.getString("left", 0)).isEqualTo("L");
    assertThat(result.getString("right", 0)).isEqualTo("R");
    assertThat(reader.read()).isNull();
  }

  private MessageType createSchema() {
    return MessageTypeParser.parseMessageType(
        "message Pair {\n" +
            "  required binary left (UTF8);\n" +
            "  required binary right (UTF8);\n" +
            "}");
  }

  private ParquetWriter<Group> createWriter(MessageType schema) throws IOException {
    GroupWriteSupport.setSchema(schema, conf);
    val writeSupport = new GroupWriteSupport();

    return new ParquetWriter<Group>(path, writeSupport,
        ParquetWriter.DEFAULT_COMPRESSION_CODEC_NAME,
        ParquetWriter.DEFAULT_BLOCK_SIZE,
        ParquetWriter.DEFAULT_PAGE_SIZE,
        ParquetWriter.DEFAULT_PAGE_SIZE, // Dictionary page size
        ParquetWriter.DEFAULT_IS_DICTIONARY_ENABLED,
        ParquetWriter.DEFAULT_IS_VALIDATING_ENABLED,
        ParquetProperties.WriterVersion.PARQUET_1_0, conf);
  }

  private ParquetReader<Group> createReader() throws IOException {
    val readSupport = new GroupReadSupport();
    return ParquetReader.builder(readSupport, path).build();
  }

}
