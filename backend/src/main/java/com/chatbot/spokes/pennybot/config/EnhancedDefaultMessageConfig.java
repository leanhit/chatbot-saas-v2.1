package com.chatbot.spokes.pennybot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

import java.util.Map;
import java.util.HashMap;

/**
 * Enhanced Default Messages for PennyBot with more comprehensive responses
 */
@Component
@ConfigurationProperties(prefix = "pennybot.enhanced-messages")
@Data
public class EnhancedDefaultMessageConfig {
    
    private Map<String, String> greetings = new HashMap<>();
    private Map<String, String> gratitude = new HashMap<>();
    private Map<String, String> goodbye = new HashMap<>();
    private Map<String, String> identity = new HashMap<>();
    private Map<String, String> capabilities = new HashMap<>();
    private Map<String, String> fallback = new HashMap<>();
    private Map<String, String> help = new HashMap<>();
    private Map<String, String> error = new HashMap<>();
    private Map<String, String> status = new HashMap<>();
    private Map<String, String> pricing = new HashMap<>();
    
    public EnhancedDefaultMessageConfig() {
        // Enhanced Vietnamese greetings
        greetings.put("vi", "Xin chào! Tôi là trợ lý ảo thông minh. Rất vui được phục vụ bạn hôm nay! 🤖");
        greetings.put("en", "Hello! I'm your intelligent virtual assistant. Nice to serve you today! 🤖");
        
        // Enhanced gratitude responses
        gratitude.put("vi", "Rất vui vì đã giúp được bạn! 😊 Còn điều gì tôi có thể hỗ trợ thêm không ạ?");
        gratitude.put("en", "I'm glad I could help! 😊 Is there anything else I can assist you with?");
        
        // Enhanced goodbye messages
        goodbye.put("vi", "Cảm ơn đã sử dụng dịch vụ! Chúc bạn một ngày tốt lành và hẹn gặp lại! 👋");
        goodbye.put("en", "Thank you for using our service! Have a great day and see you again! 👋");
        
        // Enhanced identity responses
        identity.put("vi", "Tôi là trợ lý ảo AI thế hệ mới, được đào tạo để hỗ trợ bạn 24/7. Tôi có thể trả lời câu hỏi, hỗ trợ khách hàng và xử lý các yêu cầu phức tạp. 🧠✨");
        identity.put("en", "I'm a new generation AI virtual assistant, trained to support you 24/7. I can answer questions, provide customer support, and handle complex requests. 🧠✨");
        
        // Enhanced capabilities descriptions
        capabilities.put("vi", "Tôi có thể giúp bạn với:\n🎯 **Hỗ trợ đa kênh**: Facebook, Website, Zalo, Email\n📊 **Quản lý dữ liệu**: Phân tích và báo cáo chi tiết\n🤖 **AI Chatbot**: Trò chuyện tự nhiên thông minh\n📞 **Call Center**: Tổng đài và chuyển cuộc gọi\n🔍 **Tìm kiếm thông minh**: Tìm kiếm nhanh và chính xác\n📱 **Mobile App**: Hỗ trợ di động đầy đủ\n✨ **Cá nhân hóa**: Tùy chỉnh theo nhu cầu của bạn");
        capabilities.put("en", "I can help you with:\n🎯 **Multi-channel Support**: Facebook, Website, Zalo, Email\n📊 **Data Management**: Detailed analytics and reports\n🤖 **AI Chatbot**: Natural and intelligent conversations\n📞 **Call Center**: Switchboard and call routing\n🔍 **Smart Search**: Fast and accurate search\n📱 **Mobile App**: Full mobile support\n✨ **Personalization**: Customized to your needs");
        
        // Enhanced fallback messages
        fallback.put("vi", "Xin lỗi, tôi chưa hiểu rõ yêu cầu của bạn. Tôi có thể giúp bạn với các chủ đề:\n📦 Theo dõi đơn hàng\n🛍️ Thông tin sản phẩm\n💬 Hỗ trợ khách hàng\n📊 Báo cáo thống kê\n\nBạn vui lòng mô tả chi tiết hơn nhé!");
        fallback.put("en", "I'm sorry, I don't quite understand your request. I can help you with:\n📦 Order tracking\n🛍️ Product information\n💬 Customer support\n📊 Reports and analytics\n\nPlease provide more details!");
        
        // Help responses
        help.put("vi", "Tôi luôn sẵn sàng giúp bạn! Dịch vụ hỗ trợ của tôi bao gồm:\n\n**🛠️ Hỗ trợ kỹ thuật**\n• Kiểm tra và sửa lỗi hệ thống\n• Hướng dẫn sử dụng tính năng\n• Tối ưu hóa hiệu suất\n\n**📞 Hỗ trợ khách hàng**\n• Giải đáp thắc mắc sản phẩm\n• Hướng dẫn đặt hàng\n• Xử lý khiếu nại và đổi trả\n\n**📊 Thống kê và báo cáo**\n• Xuất báo cáo doanh thu\n• Phân tích dữ liệu sử dụng\n• Theo dõi hiệu suất dịch vụ\n\nBạn cần hỗ trợ vấn đề nào ạ?");
        help.put("en", "I'm always here to help you! My support services include:\n\n**🛠️ Technical Support**\n• System troubleshooting and fixes\n• Feature usage guidance\n• Performance optimization\n\n**📞 Customer Support**\n• Product inquiries\n• Order assistance\n• Returns and exchanges\n\n**📊 Analytics and Reports**\n• Revenue reports\n• Usage data analysis\n• Service performance tracking\n\nWhat kind of support do you need?");
        
        // Error responses
        error.put("vi", "⚠️ Đã xảy ra lỗi! Tôi xin lỗi vì sự bất tiện này.\n\n**Các bước khắc phục:**\n1️⃣ Kiểm tra lại kết nối mạng\n2️⃣ Làm mới trang web (F5)\n3️⃣ Thử lại sau vài phút\n\nNếu lỗi vẫn tiếp diễn, vui lòng:\n• 📞 Gọi hotline: 1900-xxxx\n• 📧 Email: support@example.com\n• 💬 Chat trực tuyến với đội ngũ hỗ trợ\n\nCảm ơn sự kiên nhẫn của bạn!");
        error.put("en", "⚠️ An error has occurred! I apologize for this inconvenience.\n\n**Troubleshooting steps:**\n1️⃣ Check your internet connection\n2️⃣ Refresh the webpage (F5)\n3️⃣ Try again in a few minutes\n\nIf the error persists, please:\n• 📞 Call hotline: 1900-xxxx\n• 📧 Email: support@example.com\n• 💬 Live chat with our support team\n\nThank you for your patience!");
        
        // Status responses
        status.put("vi", "📊 **Trạng thái hệ thống hiện tại:**\n\n✅ **Dịch vụ core**: Hoạt động bình thường\n✅ **CSDL**: Kết nối ổn định\n✅ **API**: Phản hồi < 100ms\n✅ **Chatbot**: Sẵn sàng 24/7\n⚠️ **Bảo trì định kỳ**: 2:00-3:00 sáng hàng ngày\n\nMọi dịch vụ đều hoạt động tốt! 🟢");
        status.put("en", "📊 **Current System Status:**\n\n✅ **Core services**: Operating normally\n✅ **Database**: Stable connection\n✅ **API**: Response time < 100ms\n✅ **Chatbot**: Available 24/7\n⚠️ **Scheduled maintenance**: 2:00-3:00 AM daily\n\nAll services are running well! 🟢");
        
        // Pricing responses
        pricing.put("vi", "💰 **Thông tin giá và gói dịch vụ:**\n\n**🔥 Gói Basic (99k/tháng)**\n• 500 tin nhắn/tháng\n• Hỗ trợ Facebook, Website\n• Báo cáo cơ bản\n\n**⚡ Gói Pro (299k/tháng)**\n• 2.000 tin nhắn/tháng\n• Hỗ trợ đa kênh (FB, Web, Zalo)\n• Báo cáo nâng cao\n• API access\n\n**🚀 Gói Enterprise (599k/tháng)**\n• Không giới hạn tin nhắn\n• Hỗ trợ 24/7 qua điện thoại\n• Tùy chỉnh chatbot\n• Dedicated account manager\n\nLiên hệ sales@example.com để được tư vấn chi tiết!");
        pricing.put("en", "💰 **Pricing and Service Plans:**\n\n**🔥 Basic Plan ($99/month)**\n• 500 messages/month\n• Facebook, Website support\n• Basic reporting\n\n**⚡ Pro Plan ($299/month)**\n• 2,000 messages/month\n• Multi-channel support (FB, Web, Zalo)\n• Advanced reporting\n• API access\n\n**🚀 Enterprise Plan ($599/month)**\n• Unlimited messages\n• 24/7 phone support\n• Custom chatbot\n• Dedicated account manager\n\nContact sales@example.com for detailed consultation!");
    }
    
    public String getMessage(String type, String language) {
        Map<String, String> messages = getMessageMap(type);
        if (messages != null) {
            return messages.getOrDefault(language, messages.get("vi"));
        }
        return fallback.getOrDefault(language, fallback.get("vi"));
    }
    
    private Map<String, String> getMessageMap(String type) {
        switch (type.toLowerCase()) {
            case "greeting":
                return greetings;
            case "gratitude":
                return gratitude;
            case "goodbye":
                return goodbye;
            case "identity":
                return identity;
            case "capabilities":
                return capabilities;
            case "help":
                return help;
            case "error":
                return error;
            case "status":
                return status;
            case "pricing":
                return pricing;
            default:
                return fallback;
        }
    }
}
