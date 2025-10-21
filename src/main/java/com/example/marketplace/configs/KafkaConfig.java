package com.example.marketplace.configs;

import com.example.marketplace.dtos.events.HandleFileEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

import static com.example.marketplace.helpers.Constants.KAFKA_TOPIC_HANDLE_FILE;
import static com.example.marketplace.helpers.Constants.KAFKA_TOPIC_SEND_MAIL;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class KafkaConfig {

    // ============== BASIC CONFIGS ==============
    @Value("${spring.kafka.bootstrap-servers}")
    private String BOOTSTRAP_SERVERS;

    @Value("${spring.kafka.security.protocol:PLAINTEXT}")
    private String SECURITY_PROTOCOL;

    // ============== CONSUMER GROUP IDs ==============
    @Value("${spring.kafka.mail-consumer.group-id}")
    private String SEND_MAIL_GROUP;

    @Value("${spring.kafka.file-consumer.group-id}")
    private String FILE_STORAGE_GROUP;

    // ============== PRODUCER CONFIGS ==============
    @Value("${spring.kafka.producer.acks:all}")
    private String PRODUCER_ACKS;

    @Value("${spring.kafka.producer.retries:3}")
    private int PRODUCER_RETRIES;

    @Value("${spring.kafka.producer.retry-backoff-ms:1000}")
    private int RETRY_BACKOFF_MS;

    @Value("${spring.kafka.producer.batch-size:16384}")
    private int BATCH_SIZE;

    @Value("${spring.kafka.producer.linger-ms:10}")
    private int LINGER_MS;

    @Value("${spring.kafka.producer.compression-type:snappy}")
    private String COMPRESSION_TYPE;

    @Value("${spring.kafka.producer.enable-idempotence:true}")
    private boolean ENABLE_IDEMPOTENCE;

    // ============== CONSUMER CONFIGS ==============
    @Value("${spring.kafka.consumer.auto-offset-reset:latest}")
    private String AUTO_OFFSET_RESET;

    @Value("${spring.kafka.consumer.max-poll-records:500}")
    private int MAX_POLL_RECORDS;

    @Value("${spring.kafka.consumer.max-poll-interval-ms:300000}")
    private int MAX_POLL_INTERVAL_MS;

    @Value("${spring.kafka.consumer.session-timeout-ms:30000}")
    private int SESSION_TIMEOUT_MS;

    @Value("${spring.kafka.consumer.heartbeat-interval-ms:10000}")
    private int HEARTBEAT_INTERVAL_MS;

    // ============== LISTENER CONFIGS ==============
    @Value("${spring.kafka.listener.concurrency:3}")
    private int LISTENER_CONCURRENCY;

    // ============== TOPIC CONFIGS ==============
    @Value("${spring.kafka.topic.partitions:3}")
    private int TOPIC_PARTITIONS;

    @Value("${spring.kafka.topic.replicas:1}")
    private int TOPIC_REPLICAS;

    // ============== TRUSTED PACKAGES ==============
    @Value("${spring.kafka.consumer.trusted-packages:com.example.marketplace.dtos.events}")
    private String TRUSTED_PACKAGES;

    private static final String CLIENT_ID = "marketplace-system";

    /*_________________________________________________TOPICS-CONFIG________________________________________________________*/
    @Bean
    public NewTopic handleFileTopic() {
        return createTopic(KAFKA_TOPIC_HANDLE_FILE); // topic: handle-file
    }

    @Bean
    public NewTopic sendMailTopic() {
        return createTopic(KAFKA_TOPIC_SEND_MAIL); // topic: send-mail
    }

    protected NewTopic createTopic(String topicName) {
        return TopicBuilder.name(topicName)
                .partitions(TOPIC_PARTITIONS)
                .replicas(TOPIC_REPLICAS)
                .build();
    }

    /*_________________________________________________CONTAINER-FACTORIES________________________________________________________*/
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> sendMailFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setObservationEnabled(true);
        factory.setConsumerFactory(sendMailConsumer());
        factory.setConcurrency(LISTENER_CONCURRENCY);
        factory.getContainerProperties().setClientId(CLIENT_ID);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, HandleFileEvent> handleFileContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, HandleFileEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setObservationEnabled(true);
        factory.setConsumerFactory(handleFileConsumer());
        factory.setConcurrency(LISTENER_CONCURRENCY);
        factory.getContainerProperties().setClientId(CLIENT_ID);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    /*_________________________________________________PRODUCER-FACTORIES________________________________________________________*/
    @Bean
    public ProducerFactory<String, HandleFileEvent> handleFileProducer() {
        Map<String, Object> props = new HashMap<>(producerCommonConfigs());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, HandleFileEvent> handleFileTemplate() {
        return new KafkaTemplate<>(handleFileProducer());
    }

    @Bean
    public ProducerFactory<String, String> sendMailProducer() {
        Map<String, Object> props = new HashMap<>(producerCommonConfigs());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> sendMailTemplate() {
        return new KafkaTemplate<>(sendMailProducer());
    }

    /*_________________________________________________CONSUMER-FACTORIES________________________________________________________*/
    @Bean
    public ConsumerFactory<String, String> sendMailConsumer() {
        Map<String, Object> props = new HashMap<>(consumerCommonConfigs());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, SEND_MAIL_GROUP);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConsumerFactory<String, HandleFileEvent> handleFileConsumer() {
        Map<String, Object> props = new HashMap<>(consumerCommonConfigs());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, FILE_STORAGE_GROUP);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, TRUSTED_PACKAGES);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /*_________________________________________________ERROR-HANDLERS________________________________________________________*/
    @Bean
    public KafkaListenerErrorHandler kafkaListenerErrorHandler() {
        return (message, exception) -> {
            log.error("Error processing Kafka message: {}", message.getPayload(), exception);
            throw new RuntimeException("Error processing Kafka message", exception);
        };
    }

    /*_________________________________________________COMMON-PROPERTIES________________________________________________________*/
    private Map<String, Object> consumerCommonConfigs() {
        Map<String, Object> props = new HashMap<>(commonConfigs());

        // Deserializer configs
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Offset management
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit

        // Transaction isolation
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        // Performance tuning
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS); // max 500 message each poll
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, MAX_POLL_INTERVAL_MS);

        // Health check config
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, SESSION_TIMEOUT_MS);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, HEARTBEAT_INTERVAL_MS);

        return props;
    }

    private Map<String, Object> producerCommonConfigs() {
        Map<String, Object> props = new HashMap<>(commonConfigs());

        // Serializer configs
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Reliability configs
        props.put(ProducerConfig.ACKS_CONFIG, PRODUCER_ACKS);
        props.put(ProducerConfig.RETRIES_CONFIG, PRODUCER_RETRIES);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, RETRY_BACKOFF_MS);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, ENABLE_IDEMPOTENCE);

        // Performance tuning
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, BATCH_SIZE);
        props.put(ProducerConfig.LINGER_MS_CONFIG, LINGER_MS);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, COMPRESSION_TYPE);

        // Timeout configs
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        return props;
    }

    private Map<String, Object> commonConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL);
        return props;
    }
}
