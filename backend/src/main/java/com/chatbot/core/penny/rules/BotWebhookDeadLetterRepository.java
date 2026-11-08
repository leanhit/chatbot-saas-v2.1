package com.chatbot.core.penny.rules;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BotWebhookDeadLetterRepository extends JpaRepository<BotWebhookDeadLetter, Long> {

    List<BotWebhookDeadLetter> findByStatus(String status);

    List<BotWebhookDeadLetter> findByBotId(UUID botId);
}
