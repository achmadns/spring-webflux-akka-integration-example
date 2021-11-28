package id.co.alamisharia.simjam.actor;

import akka.actor.AbstractLoggingActor;
import id.co.alamisharia.simjam.TransactionCode;
import id.co.alamisharia.simjam.domain.Group;
import id.co.alamisharia.simjam.domain.Transaction;
import id.co.alamisharia.simjam.message.TransactionMessage;
import id.co.alamisharia.simjam.message.TransactionStatus;

/**
 * Balance management per group; candidate for akka persistence
 */
public class TransactionHandlerActor extends AbstractLoggingActor implements TransactionCode {
    private final Group group;

    public TransactionHandlerActor(Group group) {
        this.group = group;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TransactionMessage.class, t -> {
                    Double amount = t.getAmount();
                    int code = t.getTransactionCode();
                    validateAndUpdateBalance(amount, code, t.getAccount().getName());
                })
                .match(Transaction.class, t -> {
                    Double amount = t.getAmount();
                    int code = t.getCode();
                    validateAndUpdateBalance(amount, code, t.getAccountName());

                })
                .build();
    }

    private void validateAndUpdateBalance(Double amount, int code, String accountName) {
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
                } else {
                    sender().tell(TransactionStatus.INSUFFICIENT, self());
                    log().info("Group {}; {} asked for a loan {} while the current balance is {}", group.getName(), accountName, amount, currentBalance);
                }
                break;
            default:
                sender().tell(TransactionStatus.INVALID, self());
        }
        log().info("Group {} current balance: {}", group.getName(), group.getBalance());
    }
}
