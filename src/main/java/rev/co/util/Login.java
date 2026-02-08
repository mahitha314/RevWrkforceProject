package rev.co.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rev.co.DB.DBUtil;
import rev.co.menu.AdminMenu;
import rev.co.menu.EmployeeMenu;
import rev.co.menu.ManagerMenu;

import java.sql.*;
import java.util.Scanner;
public class Login {
	 private static final Logger logger =
	            LogManager.getLogger(Login.class);

	    public static void main(String[] args) {
	    	
	        Scanner sc = new Scanner(System.in);

	        while (true) {

	        	System.out.print("\n===== RevWorkForce Login =====\n");
	        	System.out.print("Enter ID: ");
	        	int empId = sc.nextInt();

	        	System.out.print("Password: ");
	        	String password = sc.next();
	        	try (Connection con = DBUtil.getConnection()) {
	        	    String sql =
	        	        "SELECT u.user_id, u.role, u.employee_id, e.name " +
	        	        "FROM users u " +
	        	        "JOIN employees e ON u.employee_id = e.employee_id " +
	        	        "WHERE u.employee_id = ? AND u.password = ?";
	        	    PreparedStatement ps = con.prepareStatement(sql);
	        	    ps.setInt(1, empId);
	        	    ps.setString(2, password);
	        	    ResultSet rs = ps.executeQuery();
	        	    if (rs.next()) {
	        	        int userId = rs.getInt("user_id");
	        	        int employeeId = rs.getInt("employee_id");
	        	        String role = rs.getString("role");
	        	        String name = rs.getString("name"); // fetch employee name
	        	        System.out.println("\nLogin Successful! Welcome " + name + " [" + role + "]\n");
	        	        logger.info("Login successful for userId {}, role {}", userId, role);
	        	        DBUtil.logAction(userId, "Logged in");
	        	        switch (role.toUpperCase()) {
	        	            case "EMPLOYEE":
	        	                EmployeeMenu.show(employeeId);
	        	                break;

	        	            case "MANAGER":
	        	                ManagerMenu.show(employeeId);
	        	                break;

	        	            case "ADMIN":
	        	                AdminMenu.show(employeeId);
	        	                break;

	        	            default:
	        	                logger.warn("Unknown role for userId {}", userId);
	        	        }

	        	    } else {
	        	        logger.warn("Invalid login attempt for employeeId {}", empId);
	        	        System.out.println("\nInvalid ID or Password! Try again.\n");
	        	    }

	        	} catch (Exception e) {
	        	    logger.error("Login error for employeeId {}", empId, e);
	        	    System.out.println("\nAn error occurred while logging in. Please try again.\n");}
	        }

	    }
}
