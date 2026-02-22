package com.chatbot;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.ComponentScan;

@EnableScheduling
@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {
    "com.chatbot.core",
    "com.chatbot.integrations",
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
