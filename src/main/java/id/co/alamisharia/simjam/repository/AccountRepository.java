package id.co.alamisharia.simjam.repository;

import id.co.alamisharia.simjam.domain.Account;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends ReactiveSortingRepository<Account, Long> {
}
