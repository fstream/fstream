package io.fstream.simulate.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("simulate")
public class SimulateProperties {

	private int seconds;
	
	private List<String> instruments = newArrayList();
	
}
