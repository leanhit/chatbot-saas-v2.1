import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DbCheck {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5435/chatbot_tenant_db";
        String user = "chatbot_user";
        String password = "chatbot_Admin_2025";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT id, email, status, expires_at FROM tenant_invitations WHERE email = 'dqctfacebook@gmail.com'";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.println("ID: " + rs.getLong("id") + 
                                       ", Email: " + rs.getString("email") + 
                                       ", Status: " + rs.getString("status") + 
                                       ", Expires: " + rs.getString("expires_at"));
                }
                if (!found) {
                    System.out.println("No records found.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
