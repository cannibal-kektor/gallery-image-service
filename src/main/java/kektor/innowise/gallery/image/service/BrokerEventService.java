package kektor.innowise.gallery.image.service;

import kektor.innowise.gallery.image.msg.LikeEventMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrokerEventService {

    RetryTemplate retryTemplate;
    KafkaTemplate<Long, LikeEventMessage> kafkaTemplate;

    @NonFinal
    @Value("${app.broker.like-event-topic}")
    String likeEventTopic;

    @Async
    @TransactionalEventListener(
            classes = LikeEventMessage.class,
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void newLikeEvent(LikeEventMessage event) {
        retryTemplate.execute(_ -> kafkaTemplate.send(likeEventTopic, event.imageId(), event).join(),
                context -> {
                    log.error("All retry attempts failed for sending like event. : ImageId: {} UserId: {} Type: {}",
                            event.imageId(), event.userId(), event.eventType(), context.getLastThrowable());
                    return null;
                });
    }

}
