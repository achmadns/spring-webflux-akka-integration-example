package id.co.alamisharia.simjam.message;

import id.co.alamisharia.simjam.domain.Account;
import id.co.alamisharia.simjam.domain.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class TransactionMessage {
    private final Account account;
    private final Group group;
    private final Double amount;
    private final LocalDateTime timestamp;
    private final int transactionType;
}
