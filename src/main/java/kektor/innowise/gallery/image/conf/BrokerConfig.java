package kektor.innowise.gallery.image.conf;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class BrokerConfig {

    @Value("${app.broker.like-event-topic}")
    String likeEventTopicName;

    @Profile("!smoke")
    @Bean
    public NewTopic likeEventTopic() {
        return TopicBuilder.name(likeEventTopicName)
                .partitions(1)
                .replicas(1)
                .build();
    }

}
