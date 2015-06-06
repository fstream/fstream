package io.fstream.simulate.core;

import java.util.Map;
import java.util.Properties;

import io.fstream.core.model.event.Event;
import io.fstream.core.util.Codec;
import io.fstream.simulate.config.KafkaProperties;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import lombok.NonNull;
import lombok.val;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class KafkaPublisher implements Publisher {

	/**
	 * Constants. 
	 */
	private static final String PUBLISH_TOPIC = "toq";
	
	/**
	 * Dependencies.
	 */
	private final Producer<String, String> producer;

	@Autowired
	public KafkaPublisher(@NonNull KafkaProperties properties) {
		this.producer = new Producer<>(createConfig(properties.getProducerProperties()));
	}

	@Override
	public void publish(Event event) {
        val key = "1"; // TODO: this should probably be a symbol
		val value = Codec.encodeText(event);
		
		val message = new KeyedMessage<String, String>(PUBLISH_TOPIC, key, value);
        
        producer.send(message);
	}

	private static ProducerConfig createConfig(Map<String, String> producerProperties) {
		val config = new ProducerConfig(createProperties(producerProperties));
		
		return config;
	}

	private static Properties createProperties(Map<String, String> producerProperties) {
		val properties = new Properties();
		properties.putAll(producerProperties);
		
		return properties;
	}

}
