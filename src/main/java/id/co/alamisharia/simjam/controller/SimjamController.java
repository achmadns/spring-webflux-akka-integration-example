package id.co.alamisharia.simjam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.co.alamisharia.simjam.domain.Account;
import id.co.alamisharia.simjam.domain.Transaction;
import id.co.alamisharia.simjam.repository.AccountRepository;
import id.co.alamisharia.simjam.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// TODO: 28/11/21 add flow for group management

@RestController
public class SimjamController {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/account")
    public Flux<Account> listAccount() {
        return accountRepository.findAll();
    }


    @PostMapping("/account")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Account> create(@RequestBody Account account) {
        return accountRepository.save(account);
    }


    @PostMapping("/transaction")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Transaction> transact(@RequestBody Transaction transaction) {
        // TODO: 28/11/21 handle transaction flow; including posting the data into MongoDB through Kafka
        return transactionRepository.save(transaction);
    }

    @GetMapping("/account/{socialNumber}/transaction")
    public Flux<Transaction> listAccount(@PathVariable Long socialNumber) {
        return transactionRepository.findBySocialNumber(socialNumber);
    }


    @GetMapping("/transaction/{from}/{to}")
    public Flux<Transaction> findTransactionBetweenDate(@PathVariable String from, @PathVariable String to) {
        // TODO: 28/11/21 find transaction between string date; validate the parameter
        return transactionRepository.findByTransactionTimestampBetween(LocalDateTime.of(LocalDate.parse(from), LocalTime.of(0, 0)), LocalDateTime.of(LocalDate.parse(to), LocalTime.of(23, 59)));
    }
}
