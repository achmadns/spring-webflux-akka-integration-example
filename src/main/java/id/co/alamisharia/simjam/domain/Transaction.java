package id.co.alamisharia.simjam.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.relational.core.mapping.Table("TRANSACTION")
public class Transaction {
    @Id
    @GeneratedValue
    @org.springframework.data.annotation.Id
    private Long id;
    private int code;
    @Column(name = "SOCIAL_NUMBER", nullable = false)
    private Long socialNumber;
    @Column(name = "ACCOUNT_NAME", nullable = false)
    private String accountName;
    @Column(name = "GROUP_ID", nullable = false)
    private Long groupId;
    @Column(name = "GROUP_NAME", nullable = false)
    private String groupName;
    @Column(name = "TRANSACTION_TIMESTAMP", columnDefinition = "TIMESTAMP")
    private LocalDateTime transactionTimestamp;
    private Double amount;

}
