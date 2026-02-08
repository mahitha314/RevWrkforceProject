package rev.co.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil {
	
	    private static final int SYSTEM_USER_ID = 1;
	    private static final int SYSTEM_EMPLOYEE_ID = 0;

	    public static Connection getConnection() throws SQLException {
	        return DriverManager.getConnection(
	                "jdbc:oracle:thin:@localhost:1521:XE",
	                "system",
	                "system"
	        );
	    }

	    private static void ensureSystemUserExists(Connection con) throws SQLException {
	        String checkSql = "SELECT user_id FROM users WHERE user_id = ?";
	        try (PreparedStatement ps = con.prepareStatement(checkSql)) {
	            ps.setInt(1, SYSTEM_USER_ID);
	            ResultSet rs = ps.executeQuery();

	            if (!rs.next()) {
	                String insertSql = "INSERT INTO users (user_id, employee_id, username, password, role) VALUES (?,?,?,?,?)";
	                try (PreparedStatement ins = con.prepareStatement(insertSql)) {
	                    ins.setInt(1, SYSTEM_USER_ID);
	                    ins.setInt(2, SYSTEM_EMPLOYEE_ID);
	                    ins.setString(3, "SYSTEM");
	                    ins.setString(4, "system@123"); // default password
	                    ins.setString(5, "ADMIN");
	                    ins.executeUpdate();
	                }
	            }
	        }
	    }

	    // CHECK IF USER EXISTS
	    private static boolean userExists(int userId, Connection con) throws SQLException {
	        String sql = "SELECT 1 FROM users WHERE user_id = ?";
	        try (PreparedStatement ps = con.prepareStatement(sql)) {
	            ps.setInt(1, userId);
	            ResultSet rs = ps.executeQuery();
	            return rs.next();
	        }
	    }

	    //SAFE AUDIT LOGGER
	    public static void logAction(int userId, String action) {

	        String sql = "INSERT INTO audit_logs (user_id, action) VALUES (?, ?)";

	        try (Connection con = getConnection()) {

	           
	            ensureSystemUserExists(con);

	            // fallback if userId is invalid
	            if (userId <= 0 || !userExists(userId, con)) {
	                userId = SYSTEM_USER_ID;
	            }

	            try (PreparedStatement ps = con.prepareStatement(sql)) {
	                ps.setInt(1, userId);
	                ps.setString(2, action);
	                ps.executeUpdate();
	            }

	        } catch (SQLException e) {
	            System.out.println("Audit log failed: " + e.getMessage());
	        }
	    
	}

}
