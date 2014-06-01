/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.model.event;

import java.io.Serializable;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Central event abstraction in the system.
 */
@JsonTypeInfo(
    use = Id.NAME,
    include = As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @Type(value = TickEvent.class, name = "TICK"),
    @Type(value = AlertEvent.class, name = "ALERT"),
    @Type(value = MetricEvent.class, name = "METRIC")
})
public interface Event extends Serializable {

  DateTime getDateTime();

  EventType getType();

}
