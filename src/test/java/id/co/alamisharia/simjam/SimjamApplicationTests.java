package id.co.alamisharia.simjam;

import id.co.alamisharia.simjam.domain.Account;
import id.co.alamisharia.simjam.domain.Group;
import id.co.alamisharia.simjam.domain.Transaction;
import id.co.alamisharia.simjam.repository.AccountRepository;
import id.co.alamisharia.simjam.repository.GroupRepository;
import id.co.alamisharia.simjam.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SimjamApplicationTests implements SimjamConstants {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private R2dbcEntityTemplate db;

    @Test
    void data_insert_experiment() {
        StepVerifier.create(db.getDatabaseClient()
                .sql("truncate account;truncate account_group;truncate transaction;").then()).verifyComplete();
        Group desa = Group.builder().name("desa").balance(0D).build();
        Mono<Group> group = groupRepository.save(desa);
        StepVerifier.create(group).assertNext(g -> assertThat(g.getId()).isNotNull()).verifyComplete();
        Account wawan = buildAccount(1L, "Wawan Setiawan",
                LocalDate.of(1990, 1, 10), "Kompleks Asia Serasi No 100", desa);
        Account teguh = buildAccount(2L, "Teguh Sudibyantoro",
                LocalDate.of(1991, 2, 10), "Jalan Pemekaran No 99", desa);
        Account joko = buildAccount(3L, "Joko Widodo,",
                LocalDate.of(1992, 3, 10), "Dusun Pisang Rt 10 Rw 20", desa);
        Flux<Account> accounts = accountRepository.saveAll(Flux.fromIterable(Arrays.asList(wawan, teguh, joko)));
        StepVerifier.create(accounts).consumeNextWith(account -> assertThat(account.getId()).isNotNull())
                .thenConsumeWhile(a -> true).verifyComplete();
        StepVerifier.create(transactionRepository.save(buildTransaction(desa, wawan, 1_000_000D)))
                .assertNext(t -> assertThat(t.getId()).isNotNull()).verifyComplete();
    }

    private Transaction buildTransaction(Group group, Account account, double amount) {
        return Transaction.builder()
                .code(DEPOSIT).socialNumber(account.getSocialNumber()).accountName(account.getName())
                .groupId(group.getId()).groupName(group.getName())
                .transactionTimestamp(LocalDateTime.of(2020, 8, 17, 9, 0))
                .amount(amount)
                .build();
    }

    private Account buildAccount(long socialNumber, String name, LocalDate dob, String address, Group group) {
        return Account.builder().socialNumber(socialNumber).name(name).dateOfBirth(dob)
                .address(address)
//                .groups(groups(group))
                .build();
    }

    private HashSet<Group> groups(Group group) {
        HashSet<Group> groups = new HashSet<>();
        groups.add(group);
        return groups;
    }

}
