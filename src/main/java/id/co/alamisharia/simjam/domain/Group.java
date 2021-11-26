package id.co.alamisharia.simjam.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.relational.core.mapping.Table("ACCOUNT_GROUP")
public class Group {
    @Id
    @GeneratedValue
    @org.springframework.data.annotation.Id
    private Long id;
    private Double balance;
    private String name;

}
