package id.co.alamisharia.simjam;

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
import id.co.alamisharia.simjam.repository.AccountRepository;
import id.co.alamisharia.simjam.repository.GroupRepository;
import id.co.alamisharia.simjam.repository.TransactionRepository;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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

    @AfterEach
    public void after() {
//        cleanData();
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
    public void deserialize() throws JsonProcessingException {
        String json = "{\"id\":null,\"social_number\":1,\"name\":\"Wawan Setiawan\",\"date_of_birth\":\"1990-01-10\",\"address\":\"Kompleks Asia Serasi No 100\"}";
        Account account = objectMapper.readValue(json, Account.class);
        assertThat(account.getId()).isNull();
        assertThat(account.getSocialNumber()).isNotNull();
        assertThat(account.getDateOfBirth()).isNotNull();
        assertThat(account.getAddress()).isNotNull();
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
