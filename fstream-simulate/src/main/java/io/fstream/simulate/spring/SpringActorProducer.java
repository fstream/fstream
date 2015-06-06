package io.fstream.simulate.spring;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationContext;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;

/**
 * An actor producer that let's spring create the Actor instances
 * Follows free (no conditions opensource) typesafe template example
 * 
 * @author bdevani
 */
@RequiredArgsConstructor
public class SpringActorProducer implements IndirectActorProducer {

	@NonNull
    private final ApplicationContext applicationContext;
	@NonNull
    private final String actorBeanName;
	@NonNull
	private final Object[] actorConstructorArgs;

    @Override
    public Actor produce() {
        return (Actor) applicationContext.getBean(actorBeanName, actorConstructorArgs);
    }

	@Override
	@SuppressWarnings("unchecked")
    public Class<? extends Actor> actorClass() {
        return (Class<? extends Actor>) applicationContext.getType(actorBeanName);
    }
	
}