package id.co.alamisharia.simjam.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import id.co.alamisharia.simjam.TestData;
import id.co.alamisharia.simjam.TransactionType;
import id.co.alamisharia.simjam.domain.Group;
import id.co.alamisharia.simjam.message.TransactionMessage;
import id.co.alamisharia.simjam.message.TransactionStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class GroupActorTest implements TestData, TransactionType {

    protected static final ActorSystem system = ActorSystem.create();
    /**
     * Must be static object if we want to use parameterized test as we need the state of the group remains after consecutive tests
     */
    private static final Group arisan = Group.builder().balance(0D).name("arisan").build();
    private static final ActorRef groupActor = system.actorOf(Props.create(GroupActor.class, () -> new GroupActor(arisan)));


    @ParameterizedTest
    @CsvFileSource(resources = "/transaction-simulation.csv", numLinesToSkip = 1)
    public void transaction_should_go_through(String accountName, int transactionType, Double amount, String date, Double expectedBalanceAfterTransaction, String expectedTransactionStatus) {
        new TestKit(system) {{
            groupActor.tell(TransactionMessage.builder()
                    .account(accountMap.get(accountName)).group(arisan).amount(amount).transactionType(transactionType)
                    .timestamp(LocalDateTime.of(LocalDate.parse(date), LocalTime.of(9, 0)))
                    .build(), getRef());
            expectMsg(Duration.ofSeconds(1), TransactionStatus.valueOf(expectedTransactionStatus));
            assertThat(arisan.getBalance()).isEqualTo(expectedBalanceAfterTransaction);
        }};

    }

}