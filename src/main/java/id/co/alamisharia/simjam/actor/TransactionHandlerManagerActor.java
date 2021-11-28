package id.co.alamisharia.simjam.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import id.co.alamisharia.simjam.domain.Group;
import id.co.alamisharia.simjam.domain.Transaction;
import id.co.alamisharia.simjam.message.TransactionMessage;

/**
 * GroupManagerActor make sure each group will have GroupActor instance and forward the transaction accordingly. This should be improved with akka-cluster mechanism in cluster environment.
 */
public class TransactionHandlerManagerActor extends AbstractLoggingActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TransactionMessage.class, t -> {
                    Group group = t.getGroup();
                    context().child(group.getName())
                            .getOrElse(() -> context()
                                    .actorOf(Props.create(TransactionHandlerActor.class, () -> new TransactionHandlerActor(group)), group.getName()))
                            .forward(t, context());
                }).match(Transaction.class, t -> context().child(t.getGroupName())
                        .getOrElse(() -> context()
                                .actorOf(Props.create(TransactionHandlerActor.class, () -> new TransactionHandlerActor(Group.builder()
                                        .name(t.getGroupName()).balance(0D).build())), t.getGroupName()))
                        .forward(t, context()))
                .build();
    }
}
