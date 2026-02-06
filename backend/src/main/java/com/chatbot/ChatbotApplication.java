package com.chatbot;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {
    "com.chatbot.modules.identity",
    "com.chatbot.modules.auth.security", 
    "com.chatbot.modules.tenant",
    "com.chatbot.modules.userInfo",
    "com.chatbot.modules.address",
    "com.chatbot.modules.app",
    "com.chatbot.modules.billing",
    "com.chatbot.modules.wallet",
    "com.chatbot.modules.messagehub",
    "com.chatbot.modules.facebook",
    "com.chatbot.modules.config",
    "com.chatbot.modules.penny", // Re-enabled for message integration
    "com.chatbot.modules.test",
    "com.chatbot.configs",
    "com.chatbot.shared"
})
@EnableAsync
public class ChatbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatbotApplication.class, args);
	}
}
