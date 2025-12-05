package kektor.innowise.gallery.image.service;

import kektor.innowise.gallery.image.msg.LikeEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrokerEventService {

    final KafkaTemplate<Long, LikeEventMessage> kafkaTemplate;

    @Value("${app.broker.like-event-topic}")
    String likeEventTopic;

    @Async
    @TransactionalEventListener(
            classes = LikeEventMessage.class,
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void newLikeEvent(LikeEventMessage event) {
        kafkaTemplate.send(likeEventTopic, event.imageId(), event)
                .whenComplete((_, error) -> {
                    if (error != null) {
                        log.error("Error while sending like event", error);
                    }
                });
    }

}
