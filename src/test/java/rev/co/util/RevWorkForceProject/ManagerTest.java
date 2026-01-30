package rev.co.util.RevWorkForceProject;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

import rev.co.Service.ManagerService;

public class ManagerTest {
	 private final int MANAGER_ID = 201;
	 @Test
	    void testViewTeam_Success() {
	        assertDoesNotThrow(() -> {
	            ManagerService.viewTeam(MANAGER_ID);
	        });
	    }
	 // TEST CASE 2: View Leave Requests
	   
	    @Test
	    void testViewLeaveRequests_Success() {
	        assertDoesNotThrow(() -> {
	            ManagerService.viewLeaveRequests(MANAGER_ID);
	        });
	    }
	    // TEST CASE 3: View Team Leave Calendar
	   
	    @Test
	    void testViewTeamLeaves_Success() {
	        assertDoesNotThrow(() -> {
	            ManagerService.viewTeamLeaves(MANAGER_ID);
	        });
	    }
	 // TEST CASE 4: View Team Attendance
	   
	    @Test
	    void testViewTeamAttendance_Success() {
	        assertDoesNotThrow(() -> {
	            ManagerService.viewTeamAttendance(MANAGER_ID);
	        });
	    }
	 // TEST CASE 5: View Company Announcements
	   
	    @Test
	    void testViewAnnouncements_Success() {
	        assertDoesNotThrow(() -> {
	            ManagerService.viewAnnouncements();
	        });
	    }
}
