package id.co.alamisharia.simjam.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.relational.core.mapping.Table("ACCOUNT")
public class Account {
    @Id
    @GeneratedValue
    @org.springframework.data.annotation.Id
    private Long id;
    @Column(name = "SOCIAL_NUMBER", unique = true, nullable = false)
    private Long socialNumber;
    private String name;
    @Column(name = "DATE_OF_BIRTH", columnDefinition = "DATE")
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;
    private String address;
    /*@OneToMany
    @JoinTable(name = "ACCOUNT_GROUP_MAPPING",
            joinColumns = {@JoinColumn(name = "SOCIAL_NUMBER", referencedColumnName = "SOCIAL_NUMBER")},
            inverseJoinColumns = {@JoinColumn(name = "GROUP_ID", referencedColumnName = "ID")})*/
//    private Set<Group> groups;
}
