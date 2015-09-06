/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.parquet;

import static parquet.schema.OriginalType.UTF8;
import static parquet.schema.PrimitiveType.PrimitiveTypeName.BINARY;
import static parquet.schema.PrimitiveType.PrimitiveTypeName.BOOLEAN;
import static parquet.schema.PrimitiveType.PrimitiveTypeName.FLOAT;
import static parquet.schema.PrimitiveType.PrimitiveTypeName.INT32;
import static parquet.schema.PrimitiveType.PrimitiveTypeName.INT64;
import static parquet.schema.Type.Repetition.OPTIONAL;
import static parquet.schema.Type.Repetition.REQUIRED;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.EventType;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Quote;
import io.fstream.core.model.event.Trade;
import io.fstream.persist.parquet.EventParquetWriter.Groups;

import java.io.Closeable;
import java.io.IOException;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.ExtensionMethod;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.joda.time.DateTime;

import parquet.column.ParquetProperties;
import parquet.example.data.Group;
import parquet.example.data.simple.SimpleGroup;
import parquet.hadoop.ParquetWriter;
import parquet.hadoop.example.GroupWriteSupport;
import parquet.hadoop.metadata.CompressionCodecName;
import parquet.schema.MessageType;
import parquet.schema.PrimitiveType;

@ExtensionMethod(Groups.class)
public class EventParquetWriter implements Closeable {

  /**
   * Configuration.
   */
  @Getter
  private final EventType type;

  /**
   * State.
   */
  @NonNull
  private final MessageType schema;
  @NonNull
  private final ParquetWriter<Group> writer;

  public EventParquetWriter(@NonNull EventType type, @NonNull Path outputFile) {
    this.type = type;
    this.schema = createSchema(type);
    this.writer = createWriter(outputFile, schema);
  }

  @SneakyThrows
  public void write(@NonNull Event event) {
    writer.write(createGroup(event));
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }

  private Group createGroup(Event event) {
    val group = new SimpleGroup(schema);
    switch (type) {
    case TRADE:
      val trade = (Trade) event;
      return group
          .appendNullable("type", trade.getType())
          .appendNullable("dateTime", trade.getDateTime())
          .appendNullable("symbol", trade.getSymbol())

          .appendNullable("buyUser", trade.getBuyUser())
          .appendNullable("sellUser", trade.getSellUser())
          .append("amount", trade.getAmount())
          .append("price", trade.getPrice())
          .append("activeBuy", trade.isActiveBuy());
    case ORDER:
      val order = (Order) event;
      return group
          .appendNullable("type", order.getType())
          .appendNullable("dateTime", order.getDateTime())
          .appendNullable("symbol", order.getSymbol())

          .appendNullable("orderType", order.getOrderType())
          .appendNullable("side", order.getSide())
          .append("oid", order.getOid())
          .append("amount", order.getAmount())
          .append("price", order.getPrice())
          .appendNullable("brokerId", order.getBrokerId())
          .appendNullable("userId", order.getUserId())
          .appendNullable("processedTime", order.getProcessedTime());
    case QUOTE:
      val quote = (Quote) event;
      return group
          .appendNullable("type", quote.getType())
          .appendNullable("dateTime", quote.getDateTime())
          .appendNullable("symbol", quote.getSymbol())

          .append("ask", quote.getAsk())
          .append("bid", quote.getBid())
          .append("mid", quote.getMid())
          .append("askAmount", quote.getAskAmount())
          .append("bidAmount", quote.getBidAmount());
    default:
      break;
    }

    throw new IllegalStateException("Unexpected event type: " + event.getType());
  }

  private MessageType createSchema(EventType type) {
    switch (type) {
    case TRADE:
      return new MessageType("trade",
          new PrimitiveType(REQUIRED, BINARY, "type", UTF8),
          new PrimitiveType(OPTIONAL, INT64, "dateTime"),
          new PrimitiveType(OPTIONAL, BINARY, "symbol", UTF8),

          new PrimitiveType(OPTIONAL, BINARY, "buyUser", UTF8),
          new PrimitiveType(OPTIONAL, BINARY, "sellUser", UTF8),
          new PrimitiveType(OPTIONAL, INT32, "amount"),
          new PrimitiveType(OPTIONAL, FLOAT, "price"),
          new PrimitiveType(OPTIONAL, BOOLEAN, "activeBuy"));
    case ORDER:
      return new MessageType("order",
          new PrimitiveType(REQUIRED, BINARY, "type", UTF8),
          new PrimitiveType(OPTIONAL, INT64, "dateTime"),
          new PrimitiveType(OPTIONAL, BINARY, "symbol", UTF8),

          new PrimitiveType(OPTIONAL, BINARY, "orderType", UTF8),
          new PrimitiveType(OPTIONAL, BINARY, "side", UTF8),
          new PrimitiveType(OPTIONAL, INT32, "oid"),
          new PrimitiveType(OPTIONAL, INT32, "amount"),
          new PrimitiveType(OPTIONAL, FLOAT, "price"),
          new PrimitiveType(OPTIONAL, BINARY, "brokerId", UTF8),
          new PrimitiveType(OPTIONAL, BINARY, "userId", UTF8),
          new PrimitiveType(OPTIONAL, INT64, "processedTime"));
    case QUOTE:
      return new MessageType("quote",
          new PrimitiveType(REQUIRED, BINARY, "type", UTF8),
          new PrimitiveType(OPTIONAL, INT64, "dateTime"),
          new PrimitiveType(OPTIONAL, BINARY, "symbol", UTF8),

          new PrimitiveType(OPTIONAL, FLOAT, "ask"),
          new PrimitiveType(OPTIONAL, FLOAT, "bid"),
          new PrimitiveType(OPTIONAL, FLOAT, "mid"),
          new PrimitiveType(OPTIONAL, INT32, "askAmount"),
          new PrimitiveType(OPTIONAL, INT32, "bidAmount"));
    default:
      break;
    }

    throw new IllegalStateException("Unexpected event type: " + type);
  }

  @SneakyThrows
  private ParquetWriter<Group> createWriter(Path outputFile, MessageType schema) {
    val conf = new Configuration();
    GroupWriteSupport.setSchema(schema, conf);
    val writeSupport = new GroupWriteSupport();

    return new ParquetWriter<Group>(outputFile, writeSupport,
        CompressionCodecName.GZIP, // Default is uncompressed
        ParquetWriter.DEFAULT_BLOCK_SIZE, // 128M
        ParquetWriter.DEFAULT_PAGE_SIZE, // 1M
        ParquetWriter.DEFAULT_PAGE_SIZE, // Dictionary page size
        ParquetWriter.DEFAULT_IS_DICTIONARY_ENABLED, // true
        ParquetWriter.DEFAULT_IS_VALIDATING_ENABLED, // valse
        ParquetProperties.WriterVersion.PARQUET_1_0, conf);
  }

  /**
   * Extension methods.
   */
  public static class Groups {

    public static Group appendNullable(Group group, String fieldName, String value) {
      if (value != null) {
        group.append(fieldName, value);
      }

      return group;
    }

    public static Group appendNullable(Group group, String fieldName, DateTime value) {
      if (value != null) {
        group.append(fieldName, value.getMillis());
      }

      return group;
    }

    public static Group appendNullable(Group group, String fieldName, Enum<?> value) {
      if (value != null) {
        group.append(fieldName, value.name());
      }

      return group;
    }

  }

}
