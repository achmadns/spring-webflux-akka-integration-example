package id.co.alamisharia.simjam.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.co.alamisharia.simjam.domain.Transaction;
import id.co.alamisharia.simjam.message.TransactionStatus;
import id.co.alamisharia.simjam.repository.TransactionRepository;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.MonoSink;

public class TransactionHandlerActor extends AbstractLoggingActor {
    private final MonoSink<Transaction> sink;
    private final Transaction transaction;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ActorRef groupManagerRef;

    public TransactionHandlerActor(MonoSink<Transaction> sink, Transaction transaction, TransactionRepository transactionRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, ActorRef groupManagerRef) {
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
                            .addCallback(result -> sink.success(t), sink::error);
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
                        default:
                    }
                })
                .build();
    }

    public static Props props(MonoSink<Transaction> sink, Transaction transaction, TransactionRepository transactionRepository,
                              KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, ActorRef groupManagerRef) {
        return Props.create(TransactionHandlerActor.class, () ->
                new TransactionHandlerActor(sink, transaction, transactionRepository, kafkaTemplate, objectMapper, groupManagerRef));
    }

    @Override
    public void preStart() throws Exception, Exception {
        groupManagerRef.tell(transaction, self());
    }
}
