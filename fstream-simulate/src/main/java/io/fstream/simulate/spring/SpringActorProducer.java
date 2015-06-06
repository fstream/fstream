package io.fstream.simulate.spring;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationContext;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;

/**
 * An actor producer that let's spring create the Actor instances Follows free
 * (no conditions opensource) typesafe template example
 * 
 * @author bdevani
 */
@RequiredArgsConstructor
public class SpringActorProducer implements IndirectActorProducer {

	@NonNull
	private final ApplicationContext applicationContext;
	@NonNull
	private final Class<? extends Actor> actorBeanClass;
	@NonNull
	private final Object[] actorConstructorArgs;

	@Override
	public Actor produce() {
		return applicationContext.getBean(actorBeanClass, actorConstructorArgs);
	}

	@Override
	public Class<? extends Actor> actorClass() {
		return actorBeanClass;
	}

}