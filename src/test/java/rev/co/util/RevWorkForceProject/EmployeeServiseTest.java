package rev.co.util.RevWorkForceProject;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import java.sql.ResultSet;
import rev.co.DB.DBUtil;
import rev.co.Service.EmployeeService;
public class EmployeeServiseTest {
	  private final int EMP_ID = 301;
	  
	   
	  @Test
	    void testViewLeaveBalance_Success() {
	        assertDoesNotThrow(() -> {
	            EmployeeService.viewLeaveBalance(EMP_ID);
	        });
	    }
//	  @Test
//	    void testApplyLeave_Success() {
//	        assertDoesNotThrow(() -> {
//	            // NOTE:
//	            // This test expects user input.
//	            // Run manually OR mock Scanner in advanced version.
//	            EmployeeService.applyLeave(EMP_ID);
//	        });
//	    }
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
}
