package id.co.alamisharia.simjam.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.co.alamisharia.simjam.domain.Transaction;
import id.co.alamisharia.simjam.message.TransactionStatus;
import id.co.alamisharia.simjam.repository.TransactionRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;
import reactor.core.publisher.MonoSink;

import java.time.Duration;

public class RequestHandlerActor extends AbstractLoggingActor {
    private final MonoSink<Transaction> sink;
    private final Transaction transaction;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ActorRef groupManagerRef;
    private Cancellable timer;

    public RequestHandlerActor(MonoSink<Transaction> sink, Transaction transaction, TransactionRepository transactionRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, ActorRef groupManagerRef) {
        this.sink = sink;
        this.transaction = transaction;
        this.transactionRepository = transactionRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.groupManagerRef = groupManagerRef;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Transaction.class, t -> {
                    kafkaTemplate.send("transaction", objectMapper.writeValueAsString(t))
                            .addCallback(new ListenableFutureCallback<>() {
                                @Override
                                public void onFailure(Throwable ex) {
                                    sink.error(ex);
                                    wrapUp();
                                }

                                @Override
                                public void onSuccess(SendResult<String, String> result) {
                                    sink.success(t);
                                    wrapUp();
                                }
                            });
                })
                .match(TransactionStatus.class, s -> {
                    switch (s) {
                        case SUCCESS:
                            transactionRepository.save(transaction)
                                    .doOnNext(t -> self().tell(t, self()))
                                    .doOnError(sink::error)
                                    .subscribe();
                            break;
                        case INSUFFICIENT:
                            sink.error(new IllegalArgumentException("Insufficient balance!"));
                            break;
                        case INVALID:
                            sink.error(new IllegalArgumentException("Amount must be greater than zero!"));
                            break;
                        default:
                            sink.error(new IllegalArgumentException("Unhandled case!"));
                    }
                })
                .matchEquals("timeout", s -> sink.error(new InterruptedException("Transaction timeout!")))
                .build();
    }

    private void wrapUp() {
        timer.cancel();
        context().stop(self());
    }

    public static Props props(MonoSink<Transaction> sink, Transaction transaction, TransactionRepository transactionRepository,
                              KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, ActorRef groupManagerRef) {
        return Props.create(RequestHandlerActor.class, () ->
                new RequestHandlerActor(sink, transaction, transactionRepository, kafkaTemplate, objectMapper, groupManagerRef));
    }

    /**
     * initialize the state and prepare the timer; the hard coded value can be migrated to configs later.
     */
    @Override
    public void preStart() {
        groupManagerRef.tell(transaction, self());
        this.timer = context().system().scheduler()
                .scheduleOnce(Duration.ofSeconds(5), self(), "timeout", context().system().dispatcher(), self());
    }
}
