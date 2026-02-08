package rev.co.util.RevWorkForceProject;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.MockUtil;

import rev.co.DB.DBUtil;
import rev.co.Service.AdminService;
import rev.co.Service.EmployeeService;
import rev.co.Service.ManagerService;
public class TestingClass {
private static final int EMP_ID = 301;
	    @Mock
	    Connection connection;
	    MockedStatic<DBUtil> mockedDBUtil;
	    @BeforeEach
	    void setUp() throws Exception {
	        MockitoAnnotations.openMocks(this);

	       
	        mockedDBUtil = mockStatic(DBUtil.class);
	        mockedDBUtil.when(DBUtil::getConnection)
	                    .thenReturn(connection);

	       
	        when(connection.prepareStatement(any()))
	                .thenReturn(preparedStatement);

	        when(preparedStatement.executeUpdate())
	                .thenReturn(1);

	        when(preparedStatement.executeQuery())
	                .thenReturn(resultSet);

	        when(resultSet.next())
	                .thenReturn(false); 
	    }

	    @Test
	    void testViewLeaveBalance_Success() {
	        assertDoesNotThrow(() -> {
	            EmployeeService.viewLeaveBalance(EMP_ID);
	        });
	    }

	    @Test
	    void testViewLeaves_Success() {
	        assertDoesNotThrow(() -> {
	            EmployeeService.viewLeaves(EMP_ID);
	        });
	    }

	    @Test
	    void testMarkAttendance_Success() {
	        assertDoesNotThrow(() -> {
	            EmployeeService.markAttendance(EMP_ID);
	        });
	    }

	    @Test
	    void testViewAttendance_Success() {
	        assertDoesNotThrow(() -> {
	            EmployeeService.viewAttendance(EMP_ID);
	        });
	    }
	    private final int MANAGER_ID = 201;
		 @Test
		    void testViewTeam_Success() {
		        assertDoesNotThrow(() -> {
		            ManagerService.viewTeam(MANAGER_ID);
		        });
		    }
		 
		 // View Leave Requests
		   
		    @Test
		    void testViewLeaveRequests_Success() {
		        assertDoesNotThrow(() -> {
		            ManagerService.viewLeaveRequests(MANAGER_ID);
		        });
		    }
		    // View Team Leave Calendar
		   
		    @Test
		    void testViewTeamLeaves_Success() {
		        assertDoesNotThrow(() -> {
		            ManagerService.viewTeamLeaves(MANAGER_ID);
		        });
		    }
		 // View Team Attendance
		   
		    @Test
		    void testViewTeamAttendance_Success() {
		        assertDoesNotThrow(() -> {
		            ManagerService.viewTeamAttendance(MANAGER_ID);
		        });
		    }
		 // View Company Announcements
		    @Mock
		    PreparedStatement preparedStatement;

		    @Test
		    void testViewAnnouncements_Success() {
		        assertDoesNotThrow(() -> {
		            ManagerService.viewAnnouncements();
		        });
		    }
		    @Test
		    void testAddEmployee_DoesNotCrash() {
		        String input =
		                "101\n" +
		                "Test User\n" +
		                "test@mail.com\n" +
		                "9999999999\n" +
		                "Test Address\n" +
		                "2000-01-01\n" +
		                "2024-01-01\n" +
		                "1\n" +
		                "1\n" +
		                "0\n" +
		                "30000\n";

		        System.setIn(new ByteArrayInputStream(input.getBytes()));
		    }

		    @Mock
		    ResultSet resultSet;
		    //  VIEW EMPLOYEES
		 
		    @Test
		    void testViewAllPerformanceReviews_DoesNotCrash() {
		        assertDoesNotThrow(() -> AdminService.viewAllPerformanceReviews(1));
		    }

		    //  UPDATE EMPLOYEE STATUS
		   
		    @Test
		    void testUpdateEmployeeStatus_InvalidStatus() {
		        String input =
		                "101\n" +
		                "WRONG\n";

		        System.setIn(new ByteArrayInputStream(input.getBytes()));

		        assertDoesNotThrow(() -> AdminService.updateEmployeeStatus(1));
		    }

		    
		    //  ADD HOLIDAY (INVALID DATE)
		   
		    @Test
		    void testAddHoliday_InvalidDate() {
		        String input =
		                "wrong-date\n" +
		                "Festival\n";

		        System.setIn(new ByteArrayInputStream(input.getBytes()));

		        assertDoesNotThrow(() -> AdminService.addHoliday(1));
		    }
		    //  VIEW AUDIT LOGS
		    
		    @Test
		    void testViewAuditLogs_DoesNotCrash() {
		        assertDoesNotThrow(() -> AdminService.viewAuditLogs(1));
		    }

		    // LOGGER INITIALIZATION
		   
		    @Test
		    void testLoggerWorking() {
		        assertNotNull(
		            LogManager.getLogger(AdminService.class),
		            "Logger should not be null"
		        );
		    }
		    
		   
	    @AfterEach
	    void tearDown() {
	        mockedDBUtil.close();
	    }
	    

	

}
