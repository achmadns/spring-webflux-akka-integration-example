package id.co.alamisharia.simjam;

import akka.actor.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import id.co.alamisharia.simjam.domain.Account;
import id.co.alamisharia.simjam.domain.Group;
import id.co.alamisharia.simjam.domain.Transaction;
import id.co.alamisharia.simjam.message.TransactionStatus;
import id.co.alamisharia.simjam.repository.AccountRepository;
import id.co.alamisharia.simjam.repository.GroupRepository;
import id.co.alamisharia.simjam.repository.TransactionRepository;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import scala.sys.Prop;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "36000")
class SimjamApplicationTests implements TransactionCode {

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
    @Autowired
    private ObjectMapper objectMapper;
    @Value(value = "${kafka.bootstrapAddress}")
    private String bootstrapAddress;
    @Value("${kafka.groupId}")
    String groupId;
    @Autowired
    @Qualifier("groupManagerActor")
    private ActorRef groupManagerRef;
    @Autowired
    private ActorSystem system;

    public static Map<String, Account> buildAccountMap() {
        HashMap<String, Account> accounts = new HashMap<>();
        accounts.put("wawan", TestData.wawan);
        accounts.put("teguh", TestData.teguh);
        accounts.put("joko", TestData.joko);
        return accounts;
    }

    @BeforeEach
    public void before() {
        cleanData();
        TestData.desa.setId(null);
        TestData.wawan.setId(null);
        TestData.teguh.setId(null);
        TestData.joko.setId(null);
    }

    @Test
    void data_insert_experiment() {
        insertData();
    }

    private void insertData() {
        insertGroupData();
        Flux<Account> accounts = accountRepository.saveAll(Flux.fromIterable(TestData.accounts));
        StepVerifier.create(accounts).consumeNextWith(account -> assertThat(account.getId()).isNotNull())
                .thenConsumeWhile(a -> true).verifyComplete();
        StepVerifier.create(accountRepository.findById(TestData.wawan.getId()))
                .assertNext(a -> assertThat(a.getDateOfBirth()).isEqualTo(TestData.wawan.getDateOfBirth())).verifyComplete();
    }

    private void insertTransaction() {
        StepVerifier.create(transactionRepository.save(buildTransaction(TestData.desa, TestData.wawan, 1_000_000D)))
                .assertNext(t -> assertThat(t.getId()).isNotNull()).verifyComplete();
    }

    private void insertGroupData() {
        Mono<Group> group = groupRepository.save(TestData.desa);
        StepVerifier.create(group).assertNext(g -> assertThat(g.getId()).isNotNull()).verifyComplete();
    }

    private void cleanData() {
        StepVerifier.create(db.getDatabaseClient()
                .sql("truncate account;truncate account_group;truncate transaction;").then()).verifyComplete();
    }

    @Test
    public void get_all_account() {
        insertData();
        client.get().uri("/account")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Account.class)
                .hasSize(3);
    }

    @Test
    public void save_an_account() {
        client.post().uri("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(TestData.wawan)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Account.class)
                .value(a -> assertThat(a.getId()).isNotNull());
    }

    @Test
    public void save_an_account_must_fail() {
        insertData();
        TestData.wawan.setId(null);
        StepVerifier.create(client.post().uri("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(TestData.wawan)
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult(new ParameterizedTypeReference<Map<String, String>>() {
                }).getResponseBody()).assertNext(messageMap -> assertThat(messageMap.get("cause")).isEqualTo("data already exists"));
    }

    @Test
    public void save_an_account_must_fail_due_to_name_length() {
        StepVerifier.create(client.post().uri("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Account.builder().name("a").socialNumber(123465L).dateOfBirth(LocalDate.parse("1988-08-24")).build())
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult(new ParameterizedTypeReference<Map<String, String>>() {
                }).getResponseBody()).assertNext(messageMap -> assertThat(messageMap.get("cause")).isEqualTo("bad input"));
    }

    /**
     * Simulate transaction and verify that the data are inserted into MongoDB. Wait for few seconds to check the data are inserted.
     *
     * @throws InterruptedException
     */
    @Test
    public void do_transaction() throws InterruptedException {
        insertData();
        ConnectionString connString = new ConnectionString("mongodb://localhost:27017/quickstart?w=majority");
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connString).build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("quickstart");
        MongoCollection<Document> collection = database.getCollection("sink");
        StepVerifier.create(collection.deleteMany(new BasicDBObject()))
                .assertNext(deleteResult -> assertThat(deleteResult.wasAcknowledged()).isTrue()).verifyComplete();
        client.post().uri("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildTransaction(TestData.desa, TestData.wawan, 1_000_000))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Transaction.class)
                .value(t -> assertThat(t.getId()).isNotNull());
        Thread.sleep(5_000);
        final ArrayList<Document> savedTransactions = new ArrayList<>();
        StepVerifier.create(collection.find())
                .recordWith(() -> savedTransactions)
                .thenConsumeWhile(document -> true)
                .verifyComplete();
        assertThat(savedTransactions).hasSize(1);
    }

    @Test
    public void do_transaction_must_fail_due_to_negative_balance() throws InterruptedException {
        StepVerifier.create(client.post().uri("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildTransaction(TestData.desa, TestData.wawan, -1000))
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult(new ParameterizedTypeReference<Map<String, String>>() {
                }).getResponseBody()).assertNext(messageMap -> assertThat(messageMap.get("cause")).isEqualTo("bad input"));
    }

    @Test
    public void do_random_transaction() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1000);
        final SecureRandom random = new SecureRandom();
        final Scheduler scheduler = system.scheduler();
        final Group group = Group.builder().name("random").balance(0D).id(0L).build();
        LongStream.range(1, 100).forEach(id -> {
            Account account = buildAccount(id, "user#" + id, LocalDate.parse("1988-08-12"), "empty", group);
            system.actorOf(Props.create(TransactionSimulation.class,
                            () -> new TransactionSimulation(latch, random, scheduler, group, account, groupManagerRef)))
                    .tell("transact", ActorRef.noSender());
        });
        latch.await();
    }

    static class TransactionSimulation extends AbstractLoggingActor {
        private Double amount = 0D;
        private int transactionCode = DEPOSIT;

        private final CountDownLatch latch;
        private final SecureRandom random;
        private final Scheduler scheduler;
        private final Group group;
        private final Account account;
        private final ActorRef groupManagerRef;

        TransactionSimulation(CountDownLatch latch, SecureRandom random, Scheduler scheduler, Group group, Account account, ActorRef groupManagerRef) {
            this.latch = latch;
            this.random = random;
            this.scheduler = scheduler;
            this.group = group;
            this.account = account;
            this.groupManagerRef = groupManagerRef;
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("transact", s -> {
                        amount = (double) random.nextInt(10) * 1000;
                        transactionCode = random.nextBoolean() ? DEPOSIT : LOAN;
                        groupManagerRef.tell(buildTransaction(group, account, amount, transactionCode), self());
                    })
                    .match(TransactionStatus.class, status -> {
                        switch (status) {
                            case SUCCESS:
                                log().info("account {} made success {} as much as {}", account.getName(), 0 == transactionCode ? "deposit" : "loan", amount);
                                break;
                            case INSUFFICIENT:
                                log().info("account {} failed to make a loan {}", account.getName(), amount);
                                break;
                            default:
                        }
                        latch.countDown();
                        Duration nextTransactionBreakPeriod = Duration.ofMillis(random.nextInt(500));
                        scheduler.scheduleOnce(nextTransactionBreakPeriod, self(), "transact", context().system().dispatcher(), self());
                    })
                    .build();
        }
    }

    @Test
    public void get_transaction_between_date() {
        insertData();
        insertTransaction();
        client.get().uri("/transaction/2020-08-16/2020-08-18")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Transaction.class)
                .hasSize(1);
    }

    @Test
    public void get_transaction_of_an_account() {
        insertData();
        insertTransaction();
        client.get().uri("/account/" + TestData.wawan.getSocialNumber() + "/transaction")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Transaction.class)
                .hasSize(1);
    }

    private Transaction buildTransaction(Group group, Account account, double amount) {
        return buildTransaction(group, account, amount, DEPOSIT);
    }

    private static Transaction buildTransaction(Group group, Account account, double amount, int transactionCode) {
        return Transaction.builder()
                .code(transactionCode).socialNumber(account.getSocialNumber()).accountName(account.getName())
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
