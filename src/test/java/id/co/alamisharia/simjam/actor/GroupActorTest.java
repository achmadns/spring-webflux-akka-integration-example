package id.co.alamisharia.simjam.actor;

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
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class GroupActorTest implements TestData, TransactionType {

    private final ActorSystem system = ActorSystem.create();


    @ParameterizedTest
    @CsvFileSource(resources = "/transaction-simulation.csv", numLinesToSkip = 1)
    public void transaction_should_go_through() {
        new TestKit(system) {{
            final Group arisan = Group.builder().balance(0D).name("arisan").build();
            system.actorOf(Props.create(GroupActor.class, () -> new GroupActor(arisan))).tell(TransactionMessage.builder()
                    .account(wawan).group(arisan).amount(1_000_000D).transactionType(DEPOSIT)
                    .timestamp(LocalDateTime.of(2020, 8, 17, 9, 0))
                    .build(), getRef());
            expectMsg(Duration.ofSeconds(1), TransactionStatus.SUCCESS);
            assertThat(arisan.getBalance()).isEqualTo(1_000_000D);
        }};

    }

}