/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.model.event;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

import java.io.Serializable;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeId;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Central event abstraction in the system.
 */
@JsonTypeInfo(use = NAME, include = PROPERTY, property = "type")
@JsonSubTypes({
    @Type(value = AlertEvent.class, name = "ALERT"),
    @Type(value = MetricEvent.class, name = "METRIC"),
    @Type(value = QuoteEvent.class, name = "QUOTE"),
    @Type(value = MetricEvent.class, name = "TRADE"),
    @Type(value = MetricEvent.class, name = "ORDER")
})
public interface Event extends Serializable {

  DateTime getDateTime();

  @JsonTypeId
  EventType getType();

}
