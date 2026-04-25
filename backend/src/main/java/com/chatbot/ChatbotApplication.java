package com.chatbot;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EnableScheduling
@SpringBootApplication
@EnableAsync
@EntityScan(basePackages = {
    "com.chatbot.core.license.model",
    "com.chatbot.core.user.model",
    "com.chatbot.core.identity.model",
    "com.chatbot.core.tenant.model"
})
@ComponentScan(basePackages = {
    "com.chatbot.core",
    "com.chatbot.modules",
    "com.chatbot.shared",
    "com.chatbot.configs",
    "com.chatbot.spokes"
})
public class ChatbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatbotApplication.class, args);
	}

}
