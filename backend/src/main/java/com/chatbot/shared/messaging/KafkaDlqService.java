package com.chatbot.shared.messaging;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Inspect and replay Kafka Dead Letter Topics (*.DLT).
 * KafkaConfig's DeadLetterPublishingRecoverer sends failed records to "{topic}.DLT".
 */
@Service
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
@Slf4j
public class KafkaDlqService {

    public static final String DLT_SUFFIX = ".DLT";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AdminClient adminClient;
    private final String bootstrapServers;

    public KafkaDlqService(KafkaTemplate<String, String> kafkaTemplate,
                           @Value("${KAFKA_BOOTSTRAP_SERVERS:${SPRING_KAFKA_BOOTSTRAP_SERVERS:${spring.kafka.bootstrap-servers:localhost:9092}}}") String bootstrapServers) {
        this.kafkaTemplate = kafkaTemplate;
        this.bootstrapServers = bootstrapServers;
        Map<String, Object> adminProps = new HashMap<>();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        this.adminClient = AdminClient.create(adminProps);
    }

    /** List all dead-letter topics with their record counts (approx via end offsets). */
    public Map<String, Object> listDlqTopics() {
        try {
            Set<String> topics = adminClient.listTopics().names().get(10, TimeUnit.SECONDS);
            List<String> dltTopics = topics.stream()
                    .filter(t -> t.endsWith(DLT_SUFFIX))
                    .sorted()
                    .collect(Collectors.toList());

            List<Map<String, Object>> result = new ArrayList<>();
            for (String topic : dltTopics) {
                Map<String, Object> info = new HashMap<>();
                info.put("topic", topic);
                info.put("originalTopic", topic.substring(0, topic.length() - DLT_SUFFIX.length()));
                info.put("recordCount", countRecords(topic));
                result.add(info);
            }
            return Map.of("dlqTopics", result, "count", result.size());
        } catch (Exception e) {
            log.error("Failed to list Kafka DLQ topics", e);
            return Map.of("error", e.getMessage());
        }
    }

    /** Read the last N records from a DLQ topic. */
    public List<Map<String, Object>> readDlq(String topic, int maxRecords) {
        validateDlqTopic(topic);
        List<ConsumerRecord<String, String>> records = pollLastRecords(topic, maxRecords);
        return records.stream().map(this::toMap).collect(Collectors.toList());
    }

    /** Replay the last N records from a DLQ topic back to the original topic. */
    public Map<String, Object> replay(String topic, int maxRecords) {
        validateDlqTopic(topic);
        String originalTopic = topic.substring(0, topic.length() - DLT_SUFFIX.length());
        List<ConsumerRecord<String, String>> records = pollLastRecords(topic, maxRecords);

        int replayed = 0;
        for (ConsumerRecord<String, String> record : records) {
            ProducerRecord<String, String> pr = new ProducerRecord<>(
                    originalTopic, null, record.key(), record.value());
            // Preserve original headers where possible
            record.headers().forEach(h -> pr.headers().add(h));
            kafkaTemplate.send(pr);
            replayed++;
        }
        kafkaTemplate.flush();
        log.info("Replayed {} records from {} to {}", replayed, topic, originalTopic);
        return Map.of("replayed", replayed, "from", topic, "to", originalTopic);
    }

    private long countRecords(String topic) {
        try (KafkaConsumer<String, String> consumer = newConsumer()) {
            consumer.assign(partitionsFor(consumer, topic));
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitionsFor(consumer, topic));
            return endOffsets.values().stream().mapToLong(Long::longValue).sum();
        } catch (Exception e) {
            log.warn("Failed to count records in {}: {}", topic, e.getMessage());
            return -1;
        }
    }

    private List<ConsumerRecord<String, String>> pollLastRecords(String topic, int maxRecords) {
        List<ConsumerRecord<String, String>> result = new ArrayList<>();
        try (KafkaConsumer<String, String> consumer = newConsumer()) {
            Set<TopicPartition> partitions = partitionsFor(consumer, topic);
            consumer.assign(partitions);

            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
            Map<TopicPartition, Long> positions = new HashMap<>();
            for (Map.Entry<TopicPartition, Long> e : endOffsets.entrySet()) {
                long start = Math.max(0, e.getValue() - maxRecords);
                consumer.seek(e.getKey(), start);
                positions.put(e.getKey(), start);
            }

            long deadline = System.currentTimeMillis() + 10_000;
            while (System.currentTimeMillis() < deadline && result.size() < maxRecords) {
                ConsumerRecords<String, String> polled = consumer.poll(Duration.ofMillis(500));
                if (polled.isEmpty()) {
                    break;
                }
                for (ConsumerRecord<String, String> r : polled) {
                    result.add(r);
                }
            }
        } catch (Exception e) {
            log.error("Failed to read DLQ topic {}", topic, e);
            throw new RuntimeException("Failed to read DLQ topic: " + e.getMessage(), e);
        }
        return result;
    }

    private KafkaConsumer<String, String> newConsumer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dlq-admin-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new KafkaConsumer<>(props);
    }

    private Set<TopicPartition> partitionsFor(KafkaConsumer<String, String> consumer, String topic) {
        return consumer.partitionsFor(topic).stream()
                .map(p -> new TopicPartition(topic, p.partition()))
                .collect(Collectors.toSet());
    }

    private Map<String, Object> toMap(ConsumerRecord<String, String> r) {
        Map<String, Object> m = new HashMap<>();
        m.put("topic", r.topic());
        m.put("partition", r.partition());
        m.put("offset", r.offset());
        m.put("key", r.key());
        m.put("value", r.value());
        m.put("timestamp", r.timestamp());
        return m;
    }

    private void validateDlqTopic(String topic) {
        if (topic == null || !topic.endsWith(DLT_SUFFIX)) {
            throw new IllegalArgumentException("Topic must be a dead-letter topic ending with " + DLT_SUFFIX);
        }
    }
}
