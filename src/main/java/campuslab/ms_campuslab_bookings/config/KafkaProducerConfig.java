package campuslab.ms_campuslab_bookings.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaProducerConfig {

    @Value("${campuslab.kafka.topic-events}")
    private String eventsTopic;

    @Bean
    public NewTopic bookingsEvents() {
        return TopicBuilder.name(eventsTopic)
                .partitions(3)
                .replicas(3)
                .config("retention.ms", "432000000") // 5 dias (rango 3-7d)
                .build();
    }
}