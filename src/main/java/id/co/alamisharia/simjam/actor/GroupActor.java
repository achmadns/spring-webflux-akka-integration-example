package id.co.alamisharia.simjam.actor;

import akka.actor.AbstractLoggingActor;
import id.co.alamisharia.simjam.TransactionType;
import id.co.alamisharia.simjam.domain.Group;
import id.co.alamisharia.simjam.message.TransactionMessage;
import id.co.alamisharia.simjam.message.TransactionStatus;

public class GroupActor extends AbstractLoggingActor implements TransactionType {
    private final Group group;

    public GroupActor(Group group) {
        this.group = group;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TransactionMessage.class, t -> {
                    switch (t.getTransactionType()) {
                        case DEPOSIT:
                            group.setBalance(group.getBalance() + t.getAmount());
                            sender().tell(TransactionStatus.SUCCESS, self());
                            break;
                        case LOAN:
                            break;
                        case RETURN:
                            break;
                        default:
                    }
                })
                .build();
    }
}
