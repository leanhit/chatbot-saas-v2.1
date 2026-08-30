package com.chatbot.core.metrics.health;

import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for RabbitMQ connection.
 * Checks if RabbitMQ is responsive and can establish connections.
 */
@Component
public class RabbitMQHealthIndicator implements HealthIndicator {

    private final ConnectionFactory connectionFactory;

    public RabbitMQHealthIndicator(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Health health() {
        try {
            Connection connection = connectionFactory.createConnection();
            boolean isOpen = connection.isOpen();
            connection.close();
            
            if (isOpen) {
                return Health.up()
                    .withDetail("status", "RabbitMQ is responsive")
                    .withDetail("host", connectionFactory.getHost())
                    .withDetail("port", connectionFactory.getPort())
                    .build();
            } else {
                return Health.down()
                    .withDetail("status", "RabbitMQ connection not open")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("status", "RabbitMQ connection failed")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
