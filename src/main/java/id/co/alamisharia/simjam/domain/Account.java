package id.co.alamisharia.simjam.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.relational.core.mapping.Table("ACCOUNT")
public class Account {
    @Id
    @GeneratedValue
    @org.springframework.data.annotation.Id
    private Long id;
    @Column(name = "SOCIAL_NUMBER", unique = true, nullable = false)
    @JsonProperty("social_number")
    private Long socialNumber;
    @Size(min = 3, max = 255, message
            = "Name must be between 3 and 255 characters")
    private String name;
    @Past
    @JsonProperty("date_of_birth")
    @Column(name = "DATE_OF_BIRTH", columnDefinition = "DATE")
    private LocalDate dateOfBirth;
    private String address;
}
