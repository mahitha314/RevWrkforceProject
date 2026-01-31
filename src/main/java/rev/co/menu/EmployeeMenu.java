package rev.co.menu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rev.co.Service.EmployeeService;

import java.util.Scanner;

public class EmployeeMenu {

	private static final Logger logger = LogManager.getLogger(EmployeeMenu.class);

	public static void show(int employeeId) {

		Scanner sc = new Scanner(System.in);
		int choice;

		logger.info("Employee {} logged in", employeeId);

		do {
			logger.info("Displaying Employee Menu");

			System.out.println("\n===== EMPLOYEE MENU =====");
			System.out.println("1. View Leave Balance");
			System.out.println("2. Apply Leave");
			System.out.println("3. View My Leaves");
			System.out.println("4. Cancel Pending Leave");
			System.out.println("5. View Holidays");
			System.out.println("6. View Notifications");
			System.out.println("7. Mark Attendance");
			System.out.println("8. View My Attendance");
//	            System.out.println("9. View Performance Reviews");
			System.out.println("9. Set Goals");
			System.out.println("10. View Company Announcements");
			System.out.println("11. View My Team");
			System.out.println("0. Logout");

			System.out.print("Enter choice: ");
			choice = sc.nextInt();

			logger.info("Employee {} selected option {}", employeeId, choice);

			switch (choice) {
			case 1:
				EmployeeService.viewLeaveBalance(employeeId);
				break;
			case 2:
				EmployeeService.applyLeave(employeeId);
				break;
			case 3:
				EmployeeService.viewLeaves(employeeId);
				break;
			case 4:
				EmployeeService.cancelLeave(employeeId);
				break;
			case 5:
				EmployeeService.viewHolidays();
				break;
			case 6:
				EmployeeService.viewNotifications(employeeId);
				break;
			case 7:
				EmployeeService.markAttendance(employeeId);
				break;
			case 8:
				EmployeeService.viewAttendance(employeeId);
				break;
//	            case 9:
//	                EmployeeService.viewPerformance(employeeId);
//	                break;
			case 9:
				EmployeeService.setGoals(employeeId);
				break;
			case 10:
				EmployeeService.viewAnnouncements();
				break;

			case 0:
				logger.info("Employee {} logged out", employeeId);
				break;
			default:
				logger.warn("Invalid employee menu choice: {}", choice);
			}

		} while (choice != 0);
	}

}
