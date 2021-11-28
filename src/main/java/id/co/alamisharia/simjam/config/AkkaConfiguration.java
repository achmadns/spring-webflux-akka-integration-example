package id.co.alamisharia.simjam.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import id.co.alamisharia.simjam.actor.TransactionHandlerManagerActor;
import id.co.alamisharia.simjam.config.support.SpringExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AkkaConfiguration {

    @Bean("groupManagerActor")
    public ActorRef groupManagerActor(ActorSystem system) {
        return system.actorOf(Props.create(TransactionHandlerManagerActor.class), "groupManager");
    }

    @Bean
    public SpringExtension springExtension(ActorSystem system, ApplicationContext context) {
        SpringExtension.INSTANCE.get(system).initialize(context);
        return SpringExtension.INSTANCE;
    }

}
