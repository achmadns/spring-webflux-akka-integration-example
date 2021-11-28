package id.co.alamisharia.simjam.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import id.co.alamisharia.simjam.domain.Group;
import id.co.alamisharia.simjam.domain.Transaction;
import id.co.alamisharia.simjam.message.TransactionMessage;

public class GroupManagerActor extends AbstractLoggingActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TransactionMessage.class, t -> {
                    // TODO: 27/11/21 create the group actor if not exist, load data from database of specific group first if found
                    Group group = t.getGroup();
                    context().child(group.getName())
                            .getOrElse(() -> context()
                                    .actorOf(Props.create(GroupActor.class, () -> new GroupActor(group)), group.getName()))
                            .forward(t, context());
                }).match(Transaction.class, t -> context().child(t.getGroupName())
                        .getOrElse(() -> context()
                                .actorOf(Props.create(GroupActor.class, () -> new GroupActor(Group.builder()
                                        .name(t.getGroupName()).balance(0D).build())), t.getGroupName()))
                        .forward(t, context()))
                .build();
    }
}
