package rev.co.Service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rev.co.DB.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class EmployeeService {

	private static final Logger logger = LogManager.getLogger(EmployeeService.class);

	// ===== Leave Balance =====
	public static void viewLeaveBalance(int empId) {

		System.out.println("\n===== MY LEAVE BALANCE =====");

		try (Connection con = DBUtil.getConnection()) {

			String sql = "SELECT lt.leave_name, lb.available_days " + "FROM leave_balance lb "
					+ "JOIN leave_types lt ON lb.leave_type_id = lt.leave_type_id " + "WHERE lb.employee_id = ?";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, empId);

			ResultSet rs = ps.executeQuery();

			boolean hasData = false;

			System.out.printf("%-20s %-20s%n", "LEAVE TYPE", "AVAILABLE DAYS");
			System.out.println("--------------------------------------");

			while (rs.next()) {
				hasData = true;

				String leaveName = rs.getString("leave_name");
				int days = rs.getInt("available_days");

				// Display "No leaves available" if balance is 0 or less
				String displayValue = (days <= 0) ? "No leaves available" : String.valueOf(days);

				System.out.printf("%-20s %-20s%n", leaveName, displayValue);

				// log output
				logger.info("Employee {} | {} : {}", empId, leaveName, days);
			}

			if (!hasData) {
				System.out.println("⚠️ No leave balance found.");
			}

		} catch (Exception e) {
			System.out.println("❌ Error while viewing leave balance.");
			logger.error("Error viewing leave balance for employee {}", empId, e);
		}
	}

	// ===== Apply Leave =====
	public static void applyLeave(int empId) {

		Scanner sc = new Scanner(System.in);

		System.out.println("\n===== APPLY LEAVE =====");

		System.out.print("Enter Leave Type ID (number): ");
		int leaveTypeId;
		try {
			leaveTypeId = Integer.parseInt(sc.next());
		} catch (NumberFormatException e) {
			System.out.println("❌ Leave Type ID must be a number.");
			return;
		}

		System.out.print("Start Date (yyyy-mm-dd): ");
		String start = sc.next();

		System.out.print("End Date (yyyy-mm-dd): ");
		String end = sc.next();

		sc.nextLine();
		System.out.print("Reason: ");
		String reason = sc.nextLine();

		LocalDate startDate = LocalDate.parse(start);
		LocalDate endDate = LocalDate.parse(end);

		if (endDate.isBefore(startDate)) {
			System.out.println("❌ End date cannot be before start date.");
			return;
		}

		int requestedDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

		try (Connection con = DBUtil.getConnection()) {

			// 1️⃣ Validate leave type
			String checkSql = "SELECT COUNT(*) FROM leave_types WHERE leave_type_id = ?";
			PreparedStatement checkPs = con.prepareStatement(checkSql);
			checkPs.setInt(1, leaveTypeId);
			ResultSet rs = checkPs.executeQuery();
			rs.next();

			if (rs.getInt(1) == 0) {
				System.out.println("❌ Invalid Leave Type ID.");
				return;
			}

			// 2️⃣ Check leave balance
			String balanceSql = "SELECT available_days FROM leave_balance "
					+ "WHERE employee_id = ? AND leave_type_id = ?";

			PreparedStatement psBalance = con.prepareStatement(balanceSql);
			psBalance.setInt(1, empId);
			psBalance.setInt(2, leaveTypeId);

			ResultSet rsBalance = psBalance.executeQuery();

			if (!rsBalance.next()) {
				System.out.println("❌ Leave balance not found.");
				return;
			}

			int availableDays = rsBalance.getInt("available_days");

			if (requestedDays > availableDays) {
				System.out.println("❌ Insufficient leave balance!");
				System.out.println("Available days : " + availableDays);
				System.out.println("Requested days : " + requestedDays);
				return;
			}
			String sql = "INSERT INTO leave_requests "
					+ "(leave_id, employee_id, leave_type_id, start_date, end_date, reason, status) "
					+ "VALUES (leave_request_seq.NEXTVAL, ?, ?, ?, ?, ?, 'PENDING')";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, empId);
			ps.setInt(2, leaveTypeId);
			ps.setDate(3, java.sql.Date.valueOf(startDate));
			ps.setDate(4, java.sql.Date.valueOf(endDate));
			ps.setString(5, reason);

			ps.executeUpdate();

			System.out.println("✅ Leave applied successfully!");
			DBUtil.logAction(empId, "Applied leave from " + start + " to " + end);

		} catch (Exception e) {
			System.out.println("❌ Failed to apply leave.");
			logger.error("Error applying leave for employee {}", empId, e);
		}
	}

	// ===== View My Leaves =====
	public static void viewLeaves(int empId) {

		System.out.println("\n===== MY LEAVES =====");

		try (Connection con = DBUtil.getConnection()) {

			String sql = "SELECT leave_id, start_date, end_date, status " + "FROM leave_requests "
					+ "WHERE employee_id = ? " + "ORDER BY start_date DESC";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, empId);

			ResultSet rs = ps.executeQuery();

			boolean hasData = false;

			System.out.printf("%-10s %-15s %-15s %-15s%n", "LEAVE ID", "START DATE", "END DATE", "STATUS");
			System.out.println("-------------------------------------------------------");

			while (rs.next()) {
				hasData = true;
				System.out.printf("%-10d %-15s %-15s %-15s%n", rs.getInt("leave_id"), rs.getDate("start_date"),
						rs.getDate("end_date"), rs.getString("status"));
			}

			if (!hasData) {
				System.out.println("⚠️ No leave records found.");
			}

			logger.info("Leave history viewed for employee {}", empId);

		} catch (Exception e) {
			System.out.println("❌ Error while viewing leave history.");
			logger.error("Error viewing leaves for employee {}", empId, e);
		}
	}

	// ===== Cancel Leave =====
	public static void cancelLeave(int empId) {

		Scanner sc = new Scanner(System.in);
		System.out.print("Enter Leave ID to Cancel: ");
		int leaveId = sc.nextInt();

		try (Connection con = DBUtil.getConnection()) {

			String sql = "UPDATE leave_requests " + "SET status = 'CANCELLED' " + "WHERE leave_id = ? "
					+ "AND employee_id = ? " + "AND status = 'PENDING'";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, leaveId);
			ps.setInt(2, empId);

			int rows = ps.executeUpdate();

			if (rows > 0) {
				System.out.println("✅ Leave cancelled successfully.");
				DBUtil.logAction(empId, "Cancelled leave ID " + leaveId);
				logger.info("Employee {} cancelled leave {}", empId, leaveId);
			} else {
				System.out.println("⚠ Leave not cancelled (maybe already processed).");
				logger.warn("Employee {} failed to cancel leave {}", empId, leaveId);
			}

		} catch (Exception e) {
			System.out.println("❌ Error cancelling leave");
			logger.error("Error cancelling leave for employee {}", empId, e);
		}
	}

	// ===== Holidays =====
	public static void viewHolidays() {

		System.out.println("\n===== COMPANY HOLIDAYS =====");

		try (Connection con = DBUtil.getConnection()) {

			ResultSet rs = con.createStatement().executeQuery("SELECT holiday_date, description FROM holidays");

			boolean found = false;

			while (rs.next()) {
				found = true;
				System.out.printf("%s - %s%n", rs.getDate("holiday_date"), rs.getString("description"));
			}

			if (!found) {
				System.out.println("⚠️ No holidays found.");
			}

		} catch (Exception e) {
			System.out.println("❌ Error fetching holidays");
			logger.error("Error viewing holidays", e);
		}
	}

	// ===== Notifications =====
	public static void viewNotifications(int empId) {

		System.out.println("\n===== MY NOTIFICATIONS =====");

		try (Connection con = DBUtil.getConnection()) {

			String sql = "SELECT notification_id, type, message, is_read, created_at "
					+ "FROM notifications WHERE employee_id=? ORDER BY created_at DESC";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, empId);

			ResultSet rs = ps.executeQuery();
			boolean found = false;

			while (rs.next()) {
				found = true;
				System.out.printf("ID:%d | %s | %s | %s | %s%n", rs.getInt("notification_id"), rs.getString("type"),
						rs.getString("message"), rs.getBoolean("is_read") ? "Read" : "Unread",
						rs.getTimestamp("created_at"));
			}

			if (!found) {
				System.out.println("⚠️ No notifications.");
			}

		} catch (Exception e) {
			System.out.println("❌ Error viewing notifications");
			logger.error("Error viewing notifications", e);
		}
	}

	// ===== Attendance =====
	public static void markAttendance(int empId) {
		try (Connection con = DBUtil.getConnection()) {

			// Check if attendance already marked today
			String checkSql = "SELECT COUNT(*) FROM attendance WHERE employee_id=? AND TRUNC(attendance_date)=TRUNC(SYSDATE)";
			PreparedStatement checkPs = con.prepareStatement(checkSql);
			checkPs.setInt(1, empId);
			ResultSet rsCheck = checkPs.executeQuery();
			if (rsCheck.next() && rsCheck.getInt(1) > 0) {
				System.out.println("⚠️ Attendance already marked for today");
				return;
			}

			// Insert today's attendance using sequence
			String sql = "INSERT INTO attendance (attendance_id, employee_id, attendance_date, status) "
					+ "VALUES (ATTENDANCE_SEQ.NEXTVAL, ?, SYSDATE, 'PRESENT')";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, empId);
			ps.executeUpdate();

			DBUtil.logAction(empId, "Marked attendance");
			System.out.println("✅ Attendance marked for today");

		} catch (Exception e) {
			System.out.println("❌ Error marking attendance");
			logger.error("Error marking attendance for employee {}", empId, e);
		}
	}

	// ===== View Attendance =====

	public static void viewAttendance(int empId) {
		try (Connection con = DBUtil.getConnection()) {

			String sql = "SELECT attendance_date, status FROM attendance WHERE employee_id=? ORDER BY attendance_date DESC";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, empId);
			ResultSet rs = ps.executeQuery();

			System.out.println("\n===== MY ATTENDANCE =====");
			boolean hasRecords = false;

			while (rs.next()) {
				hasRecords = true;
				System.out.println(rs.getDate("attendance_date") + " - " + rs.getString("status"));
			}

			if (!hasRecords) {
				System.out.println("⚠️ No attendance records.");
			}

		} catch (Exception e) {
			System.out.println("❌ Error viewing attendance");
			logger.error("Error viewing attendance for employee {}", empId, e);
		}
	}

	// ===== Performance =====
	public static void viewPerformance(int empId) {

		System.out.println("\n===== PERFORMANCE REVIEWS =====");

		try (Connection con = DBUtil.getConnection()) {

			PreparedStatement ps = con.prepareStatement(
					"SELECT year, self_rating, manager_rating, status FROM performance_reviews WHERE employee_id=? ORDER BY year DESC");

			ps.setInt(1, empId);
			ResultSet rs = ps.executeQuery();

			boolean found = false;

			while (rs.next()) {
				found = true;
				System.out.printf("Year:%d | Self:%d | Manager:%d | %s%n", rs.getInt("year"), rs.getInt("self_rating"),
						rs.getInt("manager_rating"), rs.getString("status"));
			}

			if (!found) {
				System.out.println("⚠️ No performance reviews.");
			}

		} catch (Exception e) {
			System.out.println("❌ Error viewing performance");
			logger.error("Error viewing performance", e);
		}
	}

	// ===== Goals =====
	public static void setGoals(int empId) {
		Scanner sc = new Scanner(System.in);

		System.out.print("Goal Description: ");
		String desc = sc.nextLine();

		System.out.print("Deadline (yyyy-mm-dd): ");
		String deadlineStr = sc.nextLine();

		System.out.print("Priority (HIGH/MEDIUM/LOW): ");
		String priority = sc.nextLine();

		System.out.print("Success Metrics: ");
		String metrics = sc.nextLine();
		try (Connection con = DBUtil.getConnection()) {
			java.sql.Date deadline = java.sql.Date.valueOf(deadlineStr);
			String sql = "INSERT INTO goals (goal_id, employee_id, description, deadline, priority, success_metrics) "
					+ "VALUES (goal_seq.NEXTVAL, ?, ?, ?, ?, ?)";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, empId);
			ps.setString(2, desc);
			ps.setDate(3, deadline);
			ps.setString(4, priority.toUpperCase());
			ps.setString(5, metrics);
			int rows = ps.executeUpdate();
			if (rows > 0) {
				// This will show in console
				System.out.println("\n✅ Goal added successfully!");
				System.out.println("Description: " + desc);
				System.out.println("Deadline: " + deadline);
				System.out.println("Priority: " + priority.toUpperCase());
				System.out.println("Success Metrics: " + metrics + "\n");

				DBUtil.logAction(empId, "Added goal: " + desc);
			} else {
				System.out.println("\n⚠ Failed to add goal.\n");
			}

		} catch (IllegalArgumentException e) {
			System.out.println("\n❌ Invalid date format. Please use yyyy-mm-dd\n");
		} catch (Exception e) {
			System.out.println("\n❌ Error adding goal. Check logs.\n");
			e.printStackTrace();
		}
	}

	// ===== Announcements =====
	public static void viewAnnouncements() {

		System.out.println("\n===== COMPANY ANNOUNCEMENTS =====");

		try (Connection con = DBUtil.getConnection()) {

			ResultSet rs = con.createStatement()
					.executeQuery("SELECT title, message, created_on FROM announcements ORDER BY created_on DESC");
			boolean found = false;
			while (rs.next()) {
				found = true;
				System.out.printf("[%s] %s%n%s%n%n", rs.getTimestamp("created_on"), rs.getString("title"),
						rs.getString("message"));
			}
			if (!found) {
				System.out.println("⚠️ No announcements.");
			}

		} catch (Exception e) {
			System.out.println("❌ Error viewing announcements");
			logger.error("Error viewing announcements", e);
		}
	}

}
