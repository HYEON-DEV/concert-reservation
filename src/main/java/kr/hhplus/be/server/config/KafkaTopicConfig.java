package kr.hhplus.be.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public KafkaAdmin.NewTopics reservationTopics(
        @Value("${app.kafka.topics.reservation-completed}") String reservationCompletedTopic
    ) {
        return new KafkaAdmin.NewTopics(
            TopicBuilder.name(reservationCompletedTopic)
                .partitions(3)
                .replicas(1)
                .build()
        );
    }
}
