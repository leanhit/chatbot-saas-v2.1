package com.chatbot.shared.penny.providers;

import com.chatbot.shared.penny.model.PennyBot;
import com.chatbot.shared.penny.repository.PennyBotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * PromptTemplateService — Xây dựng system prompt cho LLM
 *
 * Kết hợp: bot-specific prompt template + knowledge context + conversation history
 * để tạo system prompt hoàn chỉnh gửi cho GPT/Claude.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromptTemplateService {

    private static final String DEFAULT_TEMPLATE =
        "Bạn là trợ lý AI của {business_name}.\n" +
        "{business_description}\n\n" +
        "Hướng dẫn:\n" +
        "- Trả lời ngắn gọn, lịch sự, thân thiện bằng tiếng Việt.\n" +
        "- Chỉ trả lời các câu hỏi liên quan đến doanh nghiệp và sản phẩm.\n" +
        "- Nếu không chắc chắn, hãy nói rõ và đề nghị kết nối nhân viên hỗ trợ.\n" +
        "- Không bịa đặt thông tin không có trong dữ liệu được cung cấp.\n\n" +
        "{knowledge_section}" +
        "{history_section}";

    private static final int MAX_KNOWLEDGE_CHARS = 4000;
    private static final int MAX_HISTORY_TURNS   = 5;

    private final PennyBotRepository pennyBotRepository;

    /**
     * Xây dựng system prompt hoàn chỉnh cho LLM
     *
     * @param botId           UUID của bot
     * @param knowledgeSnippets danh sách đoạn KB liên quan (từ RAG search)
     * @param conversationHistory lịch sử hội thoại (List of {user, bot} maps)
     * @return system prompt string gửi cho LLM
     */
    public String buildSystemPrompt(UUID botId,
                                    List<String> knowledgeSnippets,
                                    List<Map<String, String>> conversationHistory) {

        // Load bot config
        Optional<PennyBot> botOpt = pennyBotRepository.findById(botId);

        String businessName        = "Doanh nghiệp";
        String businessDescription = "Chúng tôi cung cấp dịch vụ hỗ trợ khách hàng chuyên nghiệp.";
        String customTemplate      = DEFAULT_TEMPLATE;

        if (botOpt.isPresent()) {
            PennyBot bot = botOpt.get();
            if (bot.getBusinessName() != null && !bot.getBusinessName().isBlank()) {
                businessName = bot.getBusinessName();
            }
            if (bot.getBusinessDescription() != null && !bot.getBusinessDescription().isBlank()) {
                businessDescription = bot.getBusinessDescription();
            }
            if (bot.getSystemPrompt() != null && !bot.getSystemPrompt().isBlank()) {
                customTemplate = bot.getSystemPrompt();
            }
        }

        // Build knowledge section
        String knowledgeSection = buildKnowledgeSection(knowledgeSnippets);

        // Build history section
        String historySection = buildHistorySection(conversationHistory);

        // Fill template
        String prompt = customTemplate
            .replace("{business_name}", businessName)
            .replace("{business_description}", businessDescription)
            .replace("{knowledge_section}", knowledgeSection)
            .replace("{history_section}", historySection);

        log.debug("📝 System prompt built ({} chars) for bot: {}", prompt.length(), botId);
        return prompt;
    }

    /**
     * Phiên bản đơn giản không cần KB hay history (dùng cho chat test)
     */
    public String buildSimpleSystemPrompt(UUID botId) {
        return buildSystemPrompt(botId, List.of(), List.of());
    }

    // ─── Private helpers ───────────────────────────────────────────────────

    private String buildKnowledgeSection(List<String> snippets) {
        if (snippets == null || snippets.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder("📚 Thông tin tham khảo:\n");
        int totalChars = 0;
        int idx = 1;

        for (String snippet : snippets) {
            if (totalChars + snippet.length() > MAX_KNOWLEDGE_CHARS) {
                log.debug("KB context truncated at {} chars", totalChars);
                break;
            }
            sb.append(idx++).append(". ").append(snippet.strip()).append("\n\n");
            totalChars += snippet.length();
        }

        sb.append("\n");
        return sb.toString();
    }

    private String buildHistorySection(List<Map<String, String>> history) {
        if (history == null || history.isEmpty()) {
            return "";
        }

        // Chỉ lấy MAX_HISTORY_TURNS turns cuối
        int fromIndex = Math.max(0, history.size() - MAX_HISTORY_TURNS);
        List<Map<String, String>> recentHistory = history.subList(fromIndex, history.size());

        StringBuilder sb = new StringBuilder("💬 Lịch sử hội thoại gần đây:\n");
        for (Map<String, String> turn : recentHistory) {
            String userMsg = turn.getOrDefault("user", "");
            String botMsg  = turn.getOrDefault("bot", "");
            if (!userMsg.isBlank()) {
                sb.append("Khách: ").append(userMsg.strip()).append("\n");
            }
            if (!botMsg.isBlank()) {
                sb.append("Bot: ").append(botMsg.strip()).append("\n");
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}
