package com.chatbot.shared.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnsCallback;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
            System.err.println("Message returned: " + returnedMessage);
        });
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("Message confirmed: " + correlationData);
            } else {
                System.err.println("Message not confirmed: " + correlationData + " cause: " + cause);
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
        return factory;
    }

    @Bean
    public TopicExchange exchange() {
        return ExchangeBuilder.topicExchange(exchangeName)
                .durable(true)
                .build();
    }

    @Bean
    public Queue defaultQueue() {
        return QueueBuilder.durable(defaultQueue)
                .withArgument("x-max-length", 10000)
                .withArgument("x-message-ttl", 3600000) // 1 hour
                .build();
    }

    @Bean
    public Queue highPriorityQueue() {
        return QueueBuilder.durable(highPriorityQueue)
                .withArgument("x-max-priority", 10)
                .withArgument("x-max-length", 1000)
                .withArgument("x-message-ttl", 1800000) // 30 minutes
                .build();
    }

    @Bean
    public Queue lowPriorityQueue() {
        return QueueBuilder.durable(lowPriorityQueue)
                .withArgument("x-max-priority", 1)
                .withArgument("x-max-length", 5000)
                .withArgument("x-message-ttl", 7200000) // 2 hours
                .build();
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(emailQueue)
                .withArgument("x-max-length", 5000)
                .withArgument("x-message-ttl", 3600000) // 1 hour
                .build();
    }

    @Bean
    public Queue smsQueue() {
        return QueueBuilder.durable(smsQueue)
                .withArgument("x-max-length", 2000)
                .withArgument("x-message-ttl", 1800000) // 30 minutes
                .build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(notificationQueue)
                .withArgument("x-max-length", 10000)
                .withArgument("x-message-ttl", 3600000) // 1 hour
                .build();
    }

    @Bean
    public Queue reportQueue() {
        return QueueBuilder.durable(reportQueue)
                .withArgument("x-max-length", 100)
                .withArgument("x-message-ttl", 86400000) // 24 hours
                .build();
    }

    @Bean
    public Queue cleanupQueue() {
        return QueueBuilder.durable(cleanupQueue)
                .withArgument("x-max-length", 50)
                .withArgument("x-message-ttl", 86400000) // 24 hours
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

    // Dead Letter Queues
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
    public Binding defaultDLQBinding() {
        return BindingBuilder.bind(defaultDLQ())
                .to(exchange())
                .with("chatbot.default.dlq");
    }

    @Bean
    public Binding highPriorityDLQBinding() {
        return BindingBuilder.bind(highPriorityDLQ())
                .to(exchange())
                .with("chatbot.high-priority.dlq");
    }

    @Bean
    public Binding lowPriorityDLQBinding() {
        return BindingBuilder.bind(lowPriorityDLQ())
                .to(exchange())
                .with("chatbot.low-priority.dlq");
    }
}
