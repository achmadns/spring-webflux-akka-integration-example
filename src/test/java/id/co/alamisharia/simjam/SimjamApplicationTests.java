package id.co.alamisharia.simjam;

import id.co.alamisharia.simjam.domain.Account;
import id.co.alamisharia.simjam.domain.Group;
import id.co.alamisharia.simjam.domain.Transaction;
import id.co.alamisharia.simjam.repository.AccountRepository;
import id.co.alamisharia.simjam.repository.GroupRepository;
import id.co.alamisharia.simjam.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SimjamApplicationTests implements TransactionType {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private R2dbcEntityTemplate db;
    @Autowired
    private WebTestClient client;

    public static Map<String, Account> buildAccountMap() {
        HashMap<String, Account> accounts = new HashMap<>();
        accounts.put("wawan", TestData.wawan);
        accounts.put("teguh", TestData.teguh);
        accounts.put("joko", TestData.joko);
        return accounts;
    }

    @Test
    void data_insert_experiment() {
        StepVerifier.create(db.getDatabaseClient()
                .sql("truncate account;truncate account_group;truncate transaction;").then()).verifyComplete();
        Mono<Group> group = groupRepository.save(TestData.desa);
        StepVerifier.create(group).assertNext(g -> assertThat(g.getId()).isNotNull()).verifyComplete();
        Flux<Account> accounts = accountRepository.saveAll(Flux.fromIterable(TestData.accounts));
        StepVerifier.create(accounts).consumeNextWith(account -> assertThat(account.getId()).isNotNull())
                .thenConsumeWhile(a -> true).verifyComplete();
        StepVerifier.create(transactionRepository.save(buildTransaction(TestData.desa, TestData.wawan, 1_000_000D)))
                .assertNext(t -> assertThat(t.getId()).isNotNull()).verifyComplete();
        StepVerifier.create(accountRepository.findById(TestData.wawan.getId()))
                .assertNext(a -> assertThat(a.getDateOfBirth()).isEqualTo(TestData.wawan.getDateOfBirth())).verifyComplete();
    }

    @Test
    public void get_account_context() {
        client.get().uri("/account")
                .accept(MediaType.TEXT_PLAIN)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(text -> assertThat(text).isEqualTo("Account context"));
    }

    private Transaction buildTransaction(Group group, Account account, double amount) {
        return Transaction.builder()
                .code(DEPOSIT).socialNumber(account.getSocialNumber()).accountName(account.getName())
                .groupId(group.getId()).groupName(group.getName())
                .transactionTimestamp(LocalDateTime.of(2020, 8, 17, 9, 0))
                .amount(amount)
                .build();
    }

    public static Account buildAccount(long socialNumber, String name, LocalDate dob, String address, Group group) {
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
