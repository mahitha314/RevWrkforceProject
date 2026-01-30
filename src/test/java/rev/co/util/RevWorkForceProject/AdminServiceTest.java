package rev.co.util.RevWorkForceProject;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.sql.*;
import static org.mockito.Mockito.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rev.co.Service.AdminService;

public class AdminServiceTest {
	
	  // ================================
    // 1️⃣ ADD EMPLOYEE (SUCCESS PATH)
    // ================================
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

        assertDoesNotThrow(() -> AdminService.addEmployee(1));
    }

    // ================================
    // 2️⃣ VIEW EMPLOYEES
    // ================================
    @Test
    void testViewEmployees_DoesNotCrash() {
        assertDoesNotThrow(() -> AdminService.viewEmployees(1));
    }

    // ================================
    // 3️⃣ UPDATE EMPLOYEE STATUS
    // ================================
    @Test
    void testUpdateEmployeeStatus_InvalidStatus() {
        String input =
                "101\n" +
                "WRONG\n";

        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> AdminService.updateEmployeeStatus(1));
    }

    // ================================
    // 4️⃣ ADD HOLIDAY (INVALID DATE)
    // ================================
    @Test
    void testAddHoliday_InvalidDate() {
        String input =
                "wrong-date\n" +
                "Festival\n";

        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> AdminService.addHoliday(1));
    }

    // ================================
    // 5️⃣ VIEW AUDIT LOGS
    // ================================
    @Test
    void testViewAuditLogs_DoesNotCrash() {
        assertDoesNotThrow(() -> AdminService.viewAuditLogs(1));
    }

    // ================================
    // 6️⃣ LOGGER INITIALIZATION
    // ================================
    @Test
    void testLoggerWorking() {
        assertNotNull(
            LogManager.getLogger(AdminService.class),
            "Logger should not be null"
        );
    }
   
}
