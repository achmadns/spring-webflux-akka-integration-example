package id.co.alamisharia.simjam.actor;

import akka.actor.AbstractLoggingActor;
import id.co.alamisharia.simjam.TransactionCode;
import id.co.alamisharia.simjam.domain.Group;
import id.co.alamisharia.simjam.domain.Transaction;
import id.co.alamisharia.simjam.message.TransactionMessage;
import id.co.alamisharia.simjam.message.TransactionStatus;

public class GroupActor extends AbstractLoggingActor implements TransactionCode {
    private final Group group;

    public GroupActor(Group group) {
        this.group = group;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TransactionMessage.class, t -> {
                    Double amount = t.getAmount();
                    int code = t.getTransactionCode();
                    validateAndUpdateBalance(amount, code);
                })
                .match(Transaction.class, t -> {
                    Double amount = t.getAmount();
                    int code = t.getCode();
                    validateAndUpdateBalance(amount, code);

                })
                .build();
    }

    private void validateAndUpdateBalance(Double amount, int code) {
        if (amount <= 0) {
            sender().tell(TransactionStatus.INVALID, self());
            return;
        }
        Double currentBalance = group.getBalance();
        switch (code) {
            case DEPOSIT:
            case RETURN:
                group.setBalance(currentBalance + amount);
                sender().tell(TransactionStatus.SUCCESS, self());
                break;
            case LOAN:
                if (amount <= currentBalance) {
                    group.setBalance(currentBalance - amount);
                    sender().tell(TransactionStatus.SUCCESS, self());
                } else
                    sender().tell(TransactionStatus.INSUFFICIENT, self());
                break;
            default:
                sender().tell(TransactionStatus.INVALID, self());
        }
    }
}
