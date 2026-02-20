package kr.hhplus.be.server.config;

import kr.hhplus.be.server.ranking.application.RankingEventPublisher;
import kr.hhplus.be.server.ranking.infrastructure.RankingEventSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisPubSubConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
        RedisConnectionFactory connectionFactory,
        RankingEventSubscriber rankingEventSubscriber
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(rankingEventSubscriber, new ChannelTopic(RankingEventPublisher.CHANNEL));
        return container;
    }
}
