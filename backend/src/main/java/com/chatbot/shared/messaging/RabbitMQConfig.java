package com.chatbot.shared.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnsCallback;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.name:chatbot.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.exchange.type:topic}")
    private String exchangeType;

    @Value("${rabbitmq.queue.default:chatbot.queue.default}")
    private String defaultQueue;

    @Value("${rabbitmq.queue.high-priority:chatbot.queue.high-priority}")
    private String highPriorityQueue;

    @Value("${rabbitmq.queue.low-priority:chatbot.queue.low-priority}")
    private String lowPriorityQueue;

    @Value("${rabbitmq.queue.email:chatbot.queue.email}")
    private String emailQueue;

    @Value("${rabbitmq.queue.sms:chatbot.queue.sms}")
    private String smsQueue;

    @Value("${rabbitmq.queue.notification:chatbot.queue.notification}")
    private String notificationQueue;

    @Value("${rabbitmq.queue.report:chatbot.queue.report}")
    private String reportQueue;

    @Value("${rabbitmq.queue.cleanup:chatbot.queue.cleanup}")
    private String cleanupQueue;

    @Value("${rabbitmq.dlx.name:chatbot.dlx}")
    private String deadLetterExchange;

    @Value("${rabbitmq.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${rabbitmq.retry.initial-interval:1000}")
    private long retryInitialInterval;

    @Value("${rabbitmq.retry.multiplier:2.0}")
    private double retryMultiplier;

    @Value("${rabbitmq.retry.max-interval:10000}")
    private long retryMaxInterval;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setMandatory(true);
        
        // Handle returned messages
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            log.error("Message returned: {}", returnedMessage);
        });
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("Message confirmed: {}", correlationData);
            } else {
                log.error("Message not confirmed: {} cause: {}", correlationData, cause);
            }
        });
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(1);
        factory.setDefaultRequeueRejected(false);
        
        // Configure retry advice with exponential backoff
        factory.setAdviceChain(
            org.springframework.amqp.rabbit.config.RetryInterceptorBuilder
                .stateless()
                .maxAttempts(maxRetryAttempts)
                .backOffOptions(retryInitialInterval, retryMultiplier, retryMaxInterval)
                .recoverer(new org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer())
                .build()
        );
        
        return factory;
    }

    @Bean
    public TopicExchange exchange() {
        return ExchangeBuilder.topicExchange(exchangeName)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(deadLetterExchange)
                .durable(true)
                .build();
    }

    @Bean
    public Queue defaultQueue() {
        return QueueBuilder.durable(defaultQueue)
                .withArgument("x-max-length", 10000)
                .withArgument("x-message-ttl", 3600000) // 1 hour
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", defaultQueue + ".dlq")
                .build();
    }

    @Bean
    public Queue highPriorityQueue() {
        return QueueBuilder.durable(highPriorityQueue)
                .withArgument("x-max-priority", 10)
                .withArgument("x-max-length", 1000)
                .withArgument("x-message-ttl", 1800000) // 30 minutes
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", highPriorityQueue + ".dlq")
                .build();
    }

    @Bean
    public Queue lowPriorityQueue() {
        return QueueBuilder.durable(lowPriorityQueue)
                .withArgument("x-max-priority", 1)
                .withArgument("x-max-length", 5000)
                .withArgument("x-message-ttl", 7200000) // 2 hours
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", lowPriorityQueue + ".dlq")
                .build();
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(emailQueue)
                .withArgument("x-max-length", 5000)
                .withArgument("x-message-ttl", 3600000) // 1 hour
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", emailQueue + ".dlq")
                .build();
    }

    @Bean
    public Queue smsQueue() {
        return QueueBuilder.durable(smsQueue)
                .withArgument("x-max-length", 2000)
                .withArgument("x-message-ttl", 1800000) // 30 minutes
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", smsQueue + ".dlq")
                .build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(notificationQueue)
                .withArgument("x-max-length", 10000)
                .withArgument("x-message-ttl", 3600000) // 1 hour
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", notificationQueue + ".dlq")
                .build();
    }

    @Bean
    public Queue reportQueue() {
        return QueueBuilder.durable(reportQueue)
                .withArgument("x-max-length", 100)
                .withArgument("x-message-ttl", 86400000) // 24 hours
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", reportQueue + ".dlq")
                .build();
    }

    @Bean
    public Queue cleanupQueue() {
        return QueueBuilder.durable(cleanupQueue)
                .withArgument("x-max-length", 50)
                .withArgument("x-message-ttl", 86400000) // 24 hours
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", cleanupQueue + ".dlq")
                .build();
    }

    // Bindings
    @Bean
    public Binding defaultQueueBinding() {
        return BindingBuilder.bind(defaultQueue())
                .to(exchange())
                .with("chatbot.default.*");
    }

    @Bean
    public Binding highPriorityQueueBinding() {
        return BindingBuilder.bind(highPriorityQueue())
                .to(exchange())
                .with("chatbot.high-priority.*");
    }

    @Bean
    public Binding lowPriorityQueueBinding() {
        return BindingBuilder.bind(lowPriorityQueue())
                .to(exchange())
                .with("chatbot.low-priority.*");
    }

    @Bean
    public Binding emailQueueBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(exchange())
                .with("chatbot.email.*");
    }

    @Bean
    public Binding smsQueueBinding() {
        return BindingBuilder.bind(smsQueue())
                .to(exchange())
                .with("chatbot.sms.*");
    }

    @Bean
    public Binding notificationQueueBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(exchange())
                .with("chatbot.notification.*");
    }

    @Bean
    public Binding reportQueueBinding() {
        return BindingBuilder.bind(reportQueue())
                .to(exchange())
                .with("chatbot.report.*");
    }

    @Bean
    public Binding cleanupQueueBinding() {
        return BindingBuilder.bind(cleanupQueue())
                .to(exchange())
                .with("chatbot.cleanup.*");
    }

    // Dead Letter Queues - Complete set for all queues
    @Bean
    public Queue defaultDLQ() {
        return QueueBuilder.durable(defaultQueue + ".dlq")
                .withArgument("x-message-ttl", 604800000) // 7 days
                .build();
    }

    @Bean
    public Queue highPriorityDLQ() {
        return QueueBuilder.durable(highPriorityQueue + ".dlq")
                .withArgument("x-message-ttl", 604800000) // 7 days
                .build();
    }

    @Bean
    public Queue lowPriorityDLQ() {
        return QueueBuilder.durable(lowPriorityQueue + ".dlq")
                .withArgument("x-message-ttl", 604800000) // 7 days
                .build();
    }

    @Bean
    public Queue emailDLQ() {
        return QueueBuilder.durable(emailQueue + ".dlq")
                .withArgument("x-message-ttl", 604800000) // 7 days
                .build();
    }

    @Bean
    public Queue smsDLQ() {
        return QueueBuilder.durable(smsQueue + ".dlq")
                .withArgument("x-message-ttl", 604800000) // 7 days
                .build();
    }

    @Bean
    public Queue notificationDLQ() {
        return QueueBuilder.durable(notificationQueue + ".dlq")
                .withArgument("x-message-ttl", 604800000) // 7 days
                .build();
    }

    @Bean
    public Queue reportDLQ() {
        return QueueBuilder.durable(reportQueue + ".dlq")
                .withArgument("x-message-ttl", 604800000) // 7 days
                .build();
    }

    @Bean
    public Queue cleanupDLQ() {
        return QueueBuilder.durable(cleanupQueue + ".dlq")
                .withArgument("x-message-ttl", 604800000) // 7 days
                .build();
    }

    // DLQ Bindings to Dead Letter Exchange
    @Bean
    public Binding defaultDLQBinding() {
        return BindingBuilder.bind(defaultDLQ())
                .to(deadLetterExchange())
                .with(defaultQueue + ".dlq");
    }

    @Bean
    public Binding highPriorityDLQBinding() {
        return BindingBuilder.bind(highPriorityDLQ())
                .to(deadLetterExchange())
                .with(highPriorityQueue + ".dlq");
    }

    @Bean
    public Binding lowPriorityDLQBinding() {
        return BindingBuilder.bind(lowPriorityDLQ())
                .to(deadLetterExchange())
                .with(lowPriorityQueue + ".dlq");
    }

    @Bean
    public Binding emailDLQBinding() {
        return BindingBuilder.bind(emailDLQ())
                .to(deadLetterExchange())
                .with(emailQueue + ".dlq");
    }

    @Bean
    public Binding smsDLQBinding() {
        return BindingBuilder.bind(smsDLQ())
                .to(deadLetterExchange())
                .with(smsQueue + ".dlq");
    }

    @Bean
    public Binding notificationDLQBinding() {
        return BindingBuilder.bind(notificationDLQ())
                .to(deadLetterExchange())
                .with(notificationQueue + ".dlq");
    }

    @Bean
    public Binding reportDLQBinding() {
        return BindingBuilder.bind(reportDLQ())
                .to(deadLetterExchange())
                .with(reportQueue + ".dlq");
    }

    @Bean
    public Binding cleanupDLQBinding() {
        return BindingBuilder.bind(cleanupDLQ())
                .to(deadLetterExchange())
                .with(cleanupQueue + ".dlq");
    }
}
