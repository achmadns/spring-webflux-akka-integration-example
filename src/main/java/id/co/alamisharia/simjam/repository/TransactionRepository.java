package id.co.alamisharia.simjam.repository;

import id.co.alamisharia.simjam.domain.Transaction;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends ReactiveSortingRepository<Transaction, Long> {
}
