package com.chatbot.shared.penny.providers.llm;

import com.chatbot.shared.penny.kb.KnowledgeChunkSearchService;
import com.chatbot.shared.penny.model.BotPersonaStyle;
import com.chatbot.shared.penny.model.PennyBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * SystemPromptBuilderService — Builds dynamic system prompts integrating RAG context and Bot Persona
 *
 * Constructs comprehensive system prompts that include:
 * - Role & Persona (business name, style, description)
 * - Custom Instructions (business-specific rules)
 * - Knowledge Base Context (RAG retrieved chunks)
 * - Response Rules (language matching, fallback behavior)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemPromptBuilderService {

    private final KnowledgeChunkSearchService knowledgeChunkSearchService;

    /**
     * Build complete system prompt for LLM
     * 
     * @param bot Bot configuration
     * @param userQuery User's current query for RAG context retrieval
     * @return Complete system prompt string
     */
    public String buildSystemPrompt(PennyBot bot, String userQuery) {
        StringBuilder prompt = new StringBuilder();

        // Section 1: Role & Persona
        appendRoleAndPersona(prompt, bot);

        // Section 2: Custom Instructions
        appendCustomInstructions(prompt, bot);

        // Section 3: Knowledge Base Context (RAG)
        appendKnowledgeBaseContext(prompt, bot, userQuery);

        // Section 4: Response Rules
        appendResponseRules(prompt, bot);

        String finalPrompt = prompt.toString();
        log.debug("Built system prompt with {} characters", finalPrompt.length());
        return finalPrompt;
    }

    /**
     * Build system prompt without RAG context (for general conversations)
     */
    public String buildSystemPromptWithoutRag(PennyBot bot) {
        return buildSystemPrompt(bot, null);
    }

    /**
     * Append Role & Persona section
     */
    private void appendRoleAndPersona(StringBuilder prompt, PennyBot bot) {
        prompt.append("[ROLE & PERSONA]\n");
        
        if (bot.getBusinessName() != null && !bot.getBusinessName().isEmpty()) {
            prompt.append("Bạn là trợ lý AI đại diện cho doanh nghiệp: ")
                 .append(bot.getBusinessName()).append(".\n");
        } else {
            prompt.append("Bạn là một trợ lý AI hữu ích và chuyên nghiệp.\n");
        }

        if (bot.getPersonaStyle() != null) {
            String styleDescription = getPersonaStyleDescription(bot.getPersonaStyle());
            prompt.append("Phong cách trò chuyện: ").append(styleDescription).append(".\n");
        }

        if (bot.getBusinessDescription() != null && !bot.getBusinessDescription().isEmpty()) {
            prompt.append("Mô tả doanh nghiệp: ").append(bot.getBusinessDescription()).append("\n");
        }

        prompt.append("\n");
    }

    /**
     * Append Custom Instructions section
     */
    private void appendCustomInstructions(StringBuilder prompt, PennyBot bot) {
        if (bot.getCustomInstructions() != null && !bot.getCustomInstructions().isEmpty()) {
            prompt.append("[CUSTOM INSTRUCTIONS]\n");
            prompt.append(bot.getCustomInstructions()).append("\n\n");
        }
    }

    /**
     * Append Knowledge Base Context section (RAG)
     */
    private void appendKnowledgeBaseContext(StringBuilder prompt, PennyBot bot, String userQuery) {
        if (userQuery == null || userQuery.isEmpty() || !knowledgeChunkSearchService.isEnabled()) {
            return;
        }

        try {
            String ragContext = knowledgeChunkSearchService.searchAndFormatContext(
                bot.getId(), bot.getTenantId(), userQuery);

            if (ragContext != null && !ragContext.isEmpty()) {
                prompt.append("[KNOWLEDGE BASE CONTEXT (RAG)]\n");
                prompt.append("Dưới đây là thông tin chính thống từ tài liệu của doanh nghiệp:\n");
                prompt.append("---\n");
                prompt.append(ragContext);
                prompt.append("---\n\n");
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve RAG context: {}", e.getMessage());
        }
    }

    /**
     * Append Response Rules section
     */
    private void appendResponseRules(StringBuilder prompt, PennyBot bot) {
        prompt.append("[RULES]\n");
        prompt.append("1. Ưu tiên trả lời dựa trên KNOWLEDGE BASE CONTEXT nếu có thông tin liên quan. ");
        prompt.append("Nếu không có thông tin trong tài liệu, hãy sử dụng kiến thức chung của bạn một cách lịch sự.\n");
        prompt.append("2. Trả lời bằng ngôn ngữ mà khách hàng sử dụng (tiếng Việt hoặc tiếng Anh).\n");
        prompt.append("3. Luôn giữ phong cách trò chuyện đã được cấu hình.\n");
        prompt.append("4. Nếu không thể trả lời câu hỏi, hãy lịch sự thông báo cho khách hàng và đề xuất họ liên hệ với nhân viên hỗ trợ.\n");

        if (bot.getFallbackMessage() != null && !bot.getFallbackMessage().isEmpty()) {
            prompt.append("5. Khi không tìm thấy câu trả lời, sử dụng thông báo mặc định: ")
                 .append(bot.getFallbackMessage()).append("\n");
        }

        prompt.append("\n");
    }

    /**
     * Get description for persona style
     */
    private String getPersonaStyleDescription(BotPersonaStyle style) {
        return switch (style) {
            case PROFESSIONAL -> "Chuyên nghiệp, trang trọng, đáng tin cậy";
            case FRIENDLY -> "Thân thiện, gần gũi, nhiệt tình";
            case ENTHUSIASTIC -> "Nhiệt huyết, năng động, tích cực";
            case HUMOROUS -> "Hài hước, vui vẻ, tạo không khí thoải mái";
            case FORMAL -> "Trang trọng, lịch sự, tuân thủ quy chuẩn";
        };
    }

    /**
     * Get greeting message for bot
     */
    public String getGreetingMessage(PennyBot bot) {
        if (bot.getGreetingMessage() != null && !bot.getGreetingMessage().isEmpty()) {
            return bot.getGreetingMessage();
        }

        // Default greeting based on persona style
        return switch (bot.getPersonaStyle()) {
            case PROFESSIONAL -> "Xin chào! Tôi là trợ lý AI của " + 
                (bot.getBusinessName() != null ? bot.getBusinessName() : "chúng tôi") + 
                ". Tôi có thể giúp gì cho bạn?";
            case FRIENDLY -> "Chào bạn! Rất vui được gặp bạn. Tôi có thể hỗ trợ gì cho bạn hôm nay?";
            case ENTHUSIASTIC -> "Xin chào! 🎉 Chào mừng bạn đến với " + 
                (bot.getBusinessName() != null ? bot.getBusinessName() : "chúng tôi") + 
                "! Tôi rất sẵn lòng giúp bạn!";
            case HUMOROUS -> "Chào bạn! 👋 Tôi đây, trợ lý AI siêu ngầu của " + 
                (bot.getBusinessName() != null ? bot.getBusinessName() : "chúng tôi") + 
                ". Hãy hỏi tôi bất cứ điều gì nhé!";
            case FORMAL -> "Kính chào quý khách. Tôi là trợ lý AI của " + 
                (bot.getBusinessName() != null ? bot.getBusinessName() : "doanh nghiệp") + 
                ". Xin mời quý khách đặt câu hỏi, tôi sẽ hỗ trợ.";
        };
    }
}
