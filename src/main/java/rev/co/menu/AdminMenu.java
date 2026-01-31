package rev.co.menu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rev.co.Service.AdminService;

import java.util.Scanner;

public class AdminMenu {
	private static final Logger logger = LogManager.getLogger(AdminMenu.class);

	public static void show(int adminId) {

		Scanner sc = new Scanner(System.in);
		int choice;

		logger.info("Admin {} logged into Admin Menu", adminId);

		do {
			logger.info("Displaying Admin Menu");

			System.out.println("\n===== ADMIN MENU =====");
			System.out.println("1. Add Employee");
			System.out.println("2. View All Employees");
			System.out.println("3. Activate / Deactivate Employee");
			System.out.println("4. Assign Manager");
			System.out.println("5. View Leave Reports");
			System.out.println("6. Add Holiday");
			System.out.println("7. Add Department");
			System.out.println("8. Add Designation");
			System.out.println("9. View Performance Reviews");
			System.out.println("10. Publish Announcement");
			System.out.println("11. Send Notification");
			System.out.println("12. View Audit Logs");
			System.out.println("0. Logout");

			System.out.print("Enter Choice: ");
			choice = sc.nextInt();

			logger.info("Admin {} selected option {}", adminId, choice);

			switch (choice) {

			case 1:
				AdminService.addEmployee(adminId);
				break;
			case 2:
				AdminService.viewEmployees(adminId);
				break;
			case 3:
				AdminService.updateEmployeeStatus(adminId);
				break;
			case 4:
				AdminService.assignManager(adminId);
				break;
			case 5:
				AdminService.viewLeaveReports(adminId);
				break;
			case 6:
				AdminService.addHoliday(adminId);
				break;
			case 7:
				AdminService.addDepartment(adminId);
				break;
			case 8:
				AdminService.addDesignation(adminId);
				break;
			case 9:
				AdminService.viewAllPerformanceReviews(adminId);
				break;
			case 10:
				AdminService.addAnnouncement(adminId);
				break;
			case 11:
				AdminService.sendNotification(adminId);
				break;
			case 12:
				AdminService.viewAuditLogs(adminId);
				break;
			case 0:
				logger.info("Admin {} logged out", adminId);
				break;
			default:
				logger.warn("Invalid Admin menu choice: {}", choice);
			}

		} while (choice != 0);
	}
}
