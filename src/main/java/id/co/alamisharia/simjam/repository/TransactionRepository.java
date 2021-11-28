package id.co.alamisharia.simjam.repository;

import id.co.alamisharia.simjam.domain.Transaction;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends ReactiveSortingRepository<Transaction, Long> {
    Flux<Transaction> findBySocialNumber(Long socialNumber);

    Flux<Transaction> findByTransactionTimestampBetween(LocalDateTime from, LocalDateTime to);
}
