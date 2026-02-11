package com.chatbot.integrations.odoo.client;

import jakarta.annotation.PostConstruct;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class OdooApiClient {

    @Value("${odoo.url}")
    private String odooUrl;

    @Value("${odoo.db}")
    private String odooDb;

    @Value("${odoo.username}")
    private String odooUsername;

    @Value("${odoo.password}")
    private String odooPassword;

    private Integer uid;

    /**
     * 📢 Phương thức kiểm tra cấu hình.
     * Chạy ngay sau khi Bean được khởi tạo (cho mục đích Debug).
     */
    @PostConstruct
    public void checkConfig() {
        //log.info("📢 DEBUG CONFIG: Odoo URL={} | DB={} | User={}", odooUrl, odooDb, odooUsername);
    }

    /**
     * Khởi tạo cấu hình XmlRpc Client chung cho các API call
     */
    private XmlRpcClientConfigImpl createClientConfig(String path) throws MalformedURLException {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(odooUrl + path));
        
        // Cấu hình quan trọng để đảm bảo client gửi request ổn định
        config.setEncoding("UTF-8"); 
        config.setConnectionTimeout(60000); // 1 phút timeout
        config.setReplyTimeout(60000); 

        return config;
    }

    /**
     * 🔑 Đăng nhập Odoo và lấy UID (user ID)
     */
    public Integer authenticate() {
        try {
            if (odooUrl == null || odooDb == null || odooUsername == null || odooPassword == null) {
                log.error("❌ Cấu hình Odoo chưa được nạp đầy đủ.");
                return null;
            }

            // Sử dụng helper function để tạo config
            XmlRpcClientConfigImpl config = createClientConfig("/xmlrpc/2/common");
            
            XmlRpcClient client = new XmlRpcClient();
            Object uidObj = client.execute(config, "authenticate", new Object[]{
                    odooDb, odooUsername, odooPassword, new HashMap<>()
            });

            Integer uidInt = null;

            // Xử lý mạnh mẽ kết quả trả về từ Odoo (có thể là Integer, hoặc Boolean/Long khi lỗi)
            if (uidObj instanceof Integer) {
                uidInt = (Integer) uidObj;
            } else if (uidObj instanceof Long) {
                uidInt = ((Long) uidObj).intValue();
            }
            
            if (uidInt != null && uidInt > 0) {
                this.uid = uidInt;
                log.info("✅ Đăng nhập Odoo thành công với UID={}", uidInt);
                return uidInt;
            } else {
                log.error("❌ Đăng nhập Odoo thất bại. Response: {}", uidObj);
                return null;
            }

        } catch (Exception e) {
            log.error("❌ Lỗi khi xác thực Odoo: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 🚀 Tạo hoặc cập nhật Lead trên Odoo.
     * Nếu PSID đã tồn tại, cập nhật record; nếu chưa, tạo mới.
     */
    public Integer createOrUpdateLead(String psid, Map<String, Object> payload) {
        try {
            if (uid == null) {
                authenticate();
                if (uid == null) {
                    log.error("❌ Không thể tạo/cập nhật Lead do xác thực Odoo thất bại hoặc UID null.");
                    return null;
                }
            }

            // Setup config cho object API
            XmlRpcClientConfigImpl models = createClientConfig("/xmlrpc/2/object");
            XmlRpcClient client = new XmlRpcClient();

            // 1️⃣ Kiểm tra xem lead có tồn tại theo x_facebook_psid không
            Object[] existingIds = (Object[]) client.execute(models, "execute_kw", new Object[]{
                    odooDb, uid, odooPassword,
                    "crm.lead", "search",
                    // Thêm trường x_facebook_psid vào payload khi tạo mới
                    new Object[]{new Object[]{new Object[]{"x_facebook_psid", "=", psid}}}
            });

            Integer leadId;
            if (existingIds.length > 0) {
                leadId = (Integer) existingIds[0];
                log.info("📝 Lead đã tồn tại trong Odoo (ID={}), tiến hành cập nhật...", leadId);

                // 2️⃣ Cập nhật lead cũ
                client.execute(models, "execute_kw", new Object[]{
                        odooDb, uid, odooPassword,
                        "crm.lead", "write",
                        new Object[]{new Object[]{leadId}, payload}
                });

            } else {
                // Thêm x_facebook_psid vào payload cho hành động create
                payload.put("x_facebook_psid", psid);
                // 3️⃣ Tạo mới lead
                leadId = (Integer) client.execute(models, "execute_kw", new Object[]{
                        odooDb, uid, odooPassword,
                        "crm.lead", "create",
                        new Object[]{payload}
                });
                log.info("🎉 Lead mới đã được tạo trong Odoo với ID={}", leadId);
            }

            return leadId;

        } catch (MalformedURLException e) {
            log.error("❌ URL Odoo không hợp lệ: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("❌ Lỗi khi tạo/cập nhật lead Odoo: {}", e.getMessage(), e);
            return null;
        }
    }
}
