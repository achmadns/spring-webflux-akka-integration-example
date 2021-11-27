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
                    if (t.getAmount() <= 0) {
                        sender().tell(TransactionStatus.INVALID, self());
                        return;
                    }
                    Double currentBalance = group.getBalance();
                    switch (t.getTransactionType()) {
                        case DEPOSIT:
                        case RETURN:
                            group.setBalance(currentBalance + t.getAmount());
                            sender().tell(TransactionStatus.SUCCESS, self());
                            break;
                        case LOAN:
                            if (t.getAmount() <= currentBalance) {
                                group.setBalance(currentBalance - t.getAmount());
                                sender().tell(TransactionStatus.SUCCESS, self());
                            } else
                                sender().tell(TransactionStatus.INSUFFICIENT, self());
                            break;
                        default:
                            sender().tell(TransactionStatus.INVALID, self());
                    }
                })
                .build();
    }
}
