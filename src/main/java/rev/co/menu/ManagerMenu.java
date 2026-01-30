package rev.co.menu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rev.co.Service.ManagerService;

import java.util.Scanner;
public class ManagerMenu {
	 private static final Logger logger =
	            LogManager.getLogger(ManagerMenu.class);

	    public static void show(int managerId) {

	        Scanner sc = new Scanner(System.in);
	        int choice;

	        logger.info("Manager {} logged in", managerId);

	        do {
	            logger.info("Displaying Manager Menu");

	            System.out.println("\n===== MANAGER MENU =====");
	            System.out.println("1. View Team Members");
	            System.out.println("2. View Leave Requests");
	            System.out.println("3. Approve / Reject Leave");
	            System.out.println("4. View Team Leave Calendar");
	            System.out.println("5. View Team Goals");
	            System.out.println("6. View Team Performance Reviews");
	            System.out.println("7. View Team Attendance");
	            System.out.println("8. Send Notification to Team");
	            System.out.println("9. View Company Announcements");
	            System.out.println("0. Logout");

	            System.out.print("Enter Choice: ");
	            choice = sc.nextInt();

	            logger.info("Manager {} selected option {}", managerId, choice);

	            switch (choice) {
	            case 1:
	                ManagerService.viewTeam(managerId);
	                break;
	            case 2:
	                ManagerService.viewLeaveRequests(managerId);
	                break;
	            case 3:
	                ManagerService.updateLeaveStatus(managerId);
	                break;
	            case 4:
	                ManagerService.viewTeamLeaves(managerId);
	                break;
	            case 5:
	                ManagerService.viewTeamGoals(managerId);
	                break;
	            case 6:
	                ManagerService.viewTeamPerformance(managerId);
	                break;
	            case 7:
	                ManagerService.viewTeamAttendance(managerId);
	                break;
	            case 8:
	                ManagerService.sendNotificationToTeam(managerId);
	                break;
	            case 9:
	                ManagerService.viewAnnouncements();
	                break;
	            case 0:
	                logger.info("Manager {} logged out", managerId);
	                break;
	            default:
	                logger.warn("Invalid manager menu choice: {}", choice);
	        }


	        } while (choice != 0);
	    }
	
}
