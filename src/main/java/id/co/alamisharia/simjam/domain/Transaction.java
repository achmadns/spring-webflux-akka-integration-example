package id.co.alamisharia.simjam.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Positive;
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
    @JsonProperty("social_number")
    private Long socialNumber;
    @Column(name = "ACCOUNT_NAME", nullable = false)
    @JsonProperty("account_name")
    private String accountName;
    @Column(name = "GROUP_ID", nullable = false)
    @JsonProperty("group_id")
    private Long groupId;
    @Column(name = "GROUP_NAME", nullable = false)
    @JsonProperty("group_name")
    private String groupName;
    @Column(name = "TRANSACTION_TIMESTAMP", columnDefinition = "TIMESTAMP")
    @JsonProperty("transaction_timestamp")
    private LocalDateTime transactionTimestamp;
    @Positive
    private Double amount;

}
