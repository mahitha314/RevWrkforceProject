package rev.co.Service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rev.co.DB.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class ManagerService {

	private static final Logger logger = LogManager.getLogger(ManagerService.class);

	// 1. View Team Members
	public static void viewTeam(int managerId) {
		try (Connection con = DBUtil.getConnection()) {

			String sql = "SELECT employee_id, name, email FROM employees WHERE manager_id=?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, managerId);
			ResultSet rs = ps.executeQuery();

			System.out.println("\n===== MY TEAM =====");
			boolean hasTeam = false;

			while (rs.next()) {
				hasTeam = true;
				System.out.println("ID: " + rs.getInt("employee_id") + " | Name: " + rs.getString("name") + " | Email: "
						+ rs.getString("email"));
			}

			if (!hasTeam) {
				System.out.println("⚠ No team members assigned to you.");
			}

		} catch (Exception e) {
			System.out.println("❌ Error viewing team");
			e.printStackTrace();
		}
	}

	// 2. View Leave Requests
	public static void viewLeaveRequests(int managerId) {
		try (Connection con = DBUtil.getConnection()) {

			String sql = "SELECT lr.leave_id, e.name, lt.leave_name, lr.start_date, lr.end_date, lr.status "
					+ "FROM leave_requests lr " + "JOIN employees e ON lr.employee_id = e.employee_id "
					+ "JOIN leave_types lt ON lr.leave_type_id = lt.leave_type_id " + "WHERE e.manager_id = ?";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, managerId);
			ResultSet rs = ps.executeQuery();

			System.out.println("\n===== LEAVE REQUESTS =====");
			boolean hasRequests = false;

			while (rs.next()) {
				hasRequests = true;
				System.out.println("LeaveID: " + rs.getInt("leave_id") + " | Employee: " + rs.getString("name")
						+ " | Type: " + rs.getString("leave_name") + " | From: " + rs.getDate("start_date") + " | To: "
						+ rs.getDate("end_date") + " | Status: " + rs.getString("status"));
			}

			if (!hasRequests) {
				System.out.println("⚠ No leave requests assigned to your team.");
			}

		} catch (Exception e) {
			System.out.println("❌ Error viewing leave requests");
			e.printStackTrace();
		}
	}

	// 3. Approve / Reject Leave
	public static void updateLeaveStatus(int managerId) {
		Scanner sc = new Scanner(System.in);

		System.out.print("Enter Leave ID: ");
		int leaveId = sc.nextInt();

		System.out.print("Approve / Reject (A/R): ");
		String choice = sc.next();

		String status = choice.equalsIgnoreCase("A") ? "APPROVED" : "REJECTED";

		try (Connection con = DBUtil.getConnection()) {
			con.setAutoCommit(false); // Transaction start

			// 1️⃣ Get leave details
			String getLeaveSql = "SELECT employee_id, leave_type_id, start_date, end_date "
					+ "FROM leave_requests WHERE leave_id = ?";
			PreparedStatement psLeave = con.prepareStatement(getLeaveSql);
			psLeave.setInt(1, leaveId);
			ResultSet rs = psLeave.executeQuery();

			if (!rs.next()) {
				System.out.println("⚠ Leave not found.");
				return;
			}

			int employeeId = rs.getInt("employee_id");
			int leaveTypeId = rs.getInt("leave_type_id");
			LocalDate startDate = rs.getDate("start_date").toLocalDate();
			LocalDate endDate = rs.getDate("end_date").toLocalDate();
			int leaveDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

			// 2️⃣ Update leave status
			String sqlUpdate = "UPDATE leave_requests SET status = ? " + "WHERE leave_id = ? AND EXISTS "
					+ "(SELECT 1 FROM employees e WHERE e.employee_id = leave_requests.employee_id AND e.manager_id = ?)";
			PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);
			psUpdate.setString(1, status);
			psUpdate.setInt(2, leaveId);
			psUpdate.setInt(3, managerId);

			int rows = psUpdate.executeUpdate();
			if (rows == 0) {
				System.out.println("⚠ You are not authorized or leave does not exist.");
				return;
			}

			// 3️⃣ Deduct leave only if APPROVED
			if (status.equals("APPROVED")) {
				String deductSql = "UPDATE leave_balance " + "SET available_days = available_days - ? "
						+ "WHERE employee_id = ? AND leave_type_id = ? " + "AND available_days >= ?";
				PreparedStatement psDeduct = con.prepareStatement(deductSql);
				psDeduct.setInt(1, leaveDays);
				psDeduct.setInt(2, employeeId);
				psDeduct.setInt(3, leaveTypeId);
				psDeduct.setInt(4, leaveDays);

				int deducted = psDeduct.executeUpdate();
				if (deducted == 0) {
					System.out.println("⚠ Leave approved but not enough balance. Manual adjustment required.");
				} else {
					System.out.println(
							"✅ Leave " + leaveId + " approved successfully" );
				}
			} else {
				System.out.println("❌ Leave " + leaveId + " rejected.");
			}

			con.commit();

		} catch (Exception e) {
			System.out.println("❌ Error updating leave status.");
			e.printStackTrace();
		}
	}
	public static void viewTeamLeaves(int managerId) {
		try (Connection con = DBUtil.getConnection()) {
			String sql = "SELECT e.name, lr.start_date, lr.end_date, lr.status " + "FROM leave_requests lr "
					+ "JOIN employees e ON lr.employee_id = e.employee_id " + "WHERE e.manager_id = ?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, managerId);
			ResultSet rs = ps.executeQuery();
			boolean hasLeaves = false;
			logger.warn("===== TEAM LEAVE CALENDAR =====");
			System.out.println("===== TEAM LEAVE CALENDAR =====");
			while (rs.next()) {
				hasLeaves = true;
				String leaveInfo = String.format("%s | %s to %s | %s", rs.getString("name"), rs.getDate("start_date"),
						rs.getDate("end_date"), rs.getString("status"));
				logger.warn(leaveInfo); // log at WARN to guarantee visibility
				System.out.println(leaveInfo); // fallback to console
			}

			if (!hasLeaves) {
				logger.warn("⚠ No leave records found for your team.");
				System.out.println("⚠ No leave records found for your team.");
			}

		} catch (Exception e) {
			logger.error("❌ Error viewing team leaves for manager {}", managerId, e);
			System.out.println("❌ Error viewing team leaves for manager " + managerId);
			e.printStackTrace();
		}
	}
	public static void viewTeamGoals(int managerId) {
		try (Connection con = DBUtil.getConnection()) {

			String sql = "SELECT e.name, g.description, g.deadline, g.priority, g.progress, g.status " + "FROM goals g "
					+ "JOIN employees e ON g.employee_id = e.employee_id " + "WHERE e.manager_id = ?";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, managerId);
			ResultSet rs = ps.executeQuery();

			boolean hasGoals = false;

			// Header - always visible
			logger.warn("===== TEAM GOALS =====");
			System.out.println("===== TEAM GOALS =====");

			while (rs.next()) {
				hasGoals = true;
				String goalInfo = String.format("%s | %s | %s | %s | %d%% | %s", rs.getString("name"),
						rs.getString("description"), rs.getDate("deadline"), rs.getString("priority"),
						rs.getInt("progress"), rs.getString("status"));

				logger.warn(goalInfo); // log at WARN to ensure it shows
				System.out.println(goalInfo); // print to console as fallback
			}

			if (!hasGoals) {
				logger.warn("⚠ No goals assigned to your team.");
				System.out.println("⚠ No goals assigned to your team.");
			}

		} catch (Exception e) {
			logger.error("❌ Error viewing team goals for manager {}", managerId, e);
			System.out.println("❌ Error viewing team goals for manager " + managerId);
			e.printStackTrace();
		}
	}

	// 6. View Team Performance Reviews
	public static void viewTeamPerformance(int managerId) {
		try (Connection con = DBUtil.getConnection()) {

			String sql = "SELECT e.name, pr.year, pr.key_deliverables, pr.accomplishments, pr.improvements, "
					+ "pr.self_rating, pr.manager_rating, pr.manager_feedback, pr.status "
					+ "FROM performance_reviews pr " + "JOIN employees e ON pr.employee_id = e.employee_id "
					+ "WHERE e.manager_id = ?";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, managerId);
			ResultSet rs = ps.executeQuery();

			boolean hasReviews = false;

			// Header always visible
			logger.warn("===== TEAM PERFORMANCE REVIEWS =====");
			System.out.println("===== TEAM PERFORMANCE REVIEWS =====");

			while (rs.next()) {
				hasReviews = true;
				String reviewInfo = String.format("Employee: %s | Year: %d | Self: %d | Manager: %d | Status: %s",
						rs.getString("name"), rs.getInt("year"), rs.getInt("self_rating"), rs.getInt("manager_rating"),
						rs.getString("status"));

				// log and print
				logger.warn(reviewInfo); // WARN ensures visibility
				System.out.println(reviewInfo);
			}

			if (!hasReviews) {
				logger.warn("⚠ No performance reviews assigned to your team.");
				System.out.println("⚠ No performance reviews assigned to your team.");
			}

		} catch (Exception e) {
			logger.error("❌ Error viewing team performance for manager {}", managerId, e);
			System.out.println("❌ Error viewing team performance for manager " + managerId);
			e.printStackTrace();
		}
	}

	// 7. View Team Attendance
	public static void viewTeamAttendance(int managerId) {
		try (Connection con = DBUtil.getConnection()) {

			// Corrected column name if your attendance table has ATTENDANCE_DATE
			String sql = "SELECT e.name, a.attendance_date, a.status " + "FROM attendance a "
					+ "JOIN employees e ON a.employee_id = e.employee_id " + "WHERE e.manager_id = ?";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, managerId);
			ResultSet rs = ps.executeQuery();

			boolean hasRecords = false;

			// Header always visible
			logger.warn("===== TEAM ATTENDANCE =====");
			System.out.println("===== TEAM ATTENDANCE =====");

			while (rs.next()) {
				hasRecords = true;

				String attendanceInfo = String.format("Employee: %s | Date: %s | Status: %s", rs.getString("name"),
						rs.getDate("attendance_date"), rs.getString("status"));

				// Log and print
				logger.warn(attendanceInfo); // WARN ensures visibility
				System.out.println(attendanceInfo);
			}

			if (!hasRecords) {
				logger.warn("⚠ No attendance records found for your team.");
				System.out.println("⚠ No attendance records found for your team.");
			}

		} catch (Exception e) {
			logger.error("❌ Error viewing team attendance for manager {}", managerId, e);
			System.out.println("❌ Error viewing team attendance for manager " + managerId);
			e.printStackTrace();
		}
	}

	// 8. Send Notification to Team
	public static void sendNotificationToTeam(int managerId) {
		Scanner sc = new Scanner(System.in);

		System.out.print("Enter Notification Message: ");
		String msg = sc.nextLine();

		System.out.print("Notification Type (LEAVE / PERFORMANCE / ANNOUNCEMENT): ");
		String type = sc.next();

		// Make input case-insensitive by converting to upper-case
		type = type.toUpperCase();

		// Optional: validate input
		if (!type.equals("LEAVE") && !type.equals("PERFORMANCE") && !type.equals("ANNOUNCEMENT")) {
			System.out.println("❌ Invalid notification type!");
			return;
		}

		try (Connection con = DBUtil.getConnection()) {

			// Use sequence for notification_id
			String sql = "INSERT INTO notifications (notification_id, employee_id, message, type) "
					+ "SELECT NOTIFICATIONS_SEQ.NEXTVAL, employee_id, ?, ? " + "FROM employees WHERE manager_id = ?";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, msg);
			ps.setString(2, type);
			ps.setInt(3, managerId);

			int rows = ps.executeUpdate();

			System.out.println("✅ " + rows + " notifications sent by manager " + managerId);
			logger.warn(rows + " notifications sent by manager " + managerId);

			DBUtil.logAction(managerId, "Sent team notification");

		} catch (Exception e) {
			System.out.println("❌ Error sending team notification");
			e.printStackTrace();
			logger.error("Error sending team notification by manager {}", managerId, e);
		}
	}

	// 9. View Company Announcements
	public static void viewAnnouncements() {
		try (Connection con = DBUtil.getConnection()) {

			ResultSet rs = con.createStatement()
					.executeQuery("SELECT title, message, created_on FROM announcements ORDER BY created_on DESC");

			System.out.println("\n===== COMPANY ANNOUNCEMENTS ====="); // console header
			logger.warn("===== COMPANY ANNOUNCEMENTS ====="); // log header

			boolean hasAnnouncements = false;

			while (rs.next()) {
				hasAnnouncements = true;
				String output = rs.getTimestamp("created_on") + " | " + rs.getString("title") + " | "
						+ rs.getString("message");

				System.out.println(output); // console
				logger.info(output); // log file
			}

			if (!hasAnnouncements) {
				System.out.println("⚠ No announcements available."); // console
				logger.warn("No announcements available."); // log
			}

		} catch (Exception e) {
			System.out.println("❌ Error viewing announcements"); // console
			e.printStackTrace();
			logger.error("Error viewing announcements", e); // log
		}
	}

}
