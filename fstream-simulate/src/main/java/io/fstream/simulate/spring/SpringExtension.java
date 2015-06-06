package io.fstream.simulate.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import akka.actor.Extension;
import akka.actor.Props;

@Component
public class SpringExtension implements Extension {

	@Autowired
    private ApplicationContext applicationContext;

    public Props props(String actorBeanName, Object... actorConstructorArgs) {
        return Props.create(SpringActorProducer.class, applicationContext, actorBeanName, actorConstructorArgs);
    }
    
}
