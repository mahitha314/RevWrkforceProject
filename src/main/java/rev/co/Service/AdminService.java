package rev.co.Service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rev.co.DB.DBUtil;

import java.sql.*;
import java.util.Scanner;
public class AdminService {
	//helper method
	private static boolean employeeIdExists(Connection con, int empId) throws SQLException {
	    String sql = "SELECT COUNT(*) FROM employees WHERE employee_id = ?";
	    try (PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setInt(1, empId);
	        ResultSet rs = ps.executeQuery();
	        rs.next();
	        return rs.getInt(1) > 0;
	    }
	}
//mail
	private static boolean isValidGmail(String email) {
	    return email != null && email.matches("^[A-Za-z0-9._%+-]+@gmail\\.com$");
	}

	

	    private static final Logger logger =
	            LogManager.getLogger(AdminService.class);

	 // ================= ADD EMPLOYEE =================
	    public static void addEmployee(int adminId) {
	        Scanner sc = new Scanner(System.in);
	        logger.info("Admin {} started Add Employee", adminId);

	        try (Connection con = DBUtil.getConnection()) {
	            con.setAutoCommit(false); // start transaction

	            // ---------------- Employee Input ----------------
	            int empId;
	            while (true) {
	                System.out.print("Employee ID: ");
	                empId = sc.nextInt();

	                if (employeeIdExists(con, empId)) {
	                    System.out.println("❌ Employee ID already exists! Please enter another ID.");
	                } else {
	                    break;
	                }
	            }

	            System.out.print("Name: ");
	            sc.nextLine();
	            String name = sc.nextLine();

	            String email;
	            while (true) {
	                System.out.print("Email (must be @gmail.com): ");
	                email = sc.next().trim();

	                if (isValidGmail(email)) {
	                    break;
	                } else {
	                    System.out.println("❌ Invalid email! Only Gmail addresses are allowed.");
	                }
	            }
	            System.out.print("Set Password for Employee: ");
	            String password = sc.next();
	            System.out.print("Add as (1-Employee / 2-Manager): ");
	            int roleChoice = sc.nextInt();
	            String role;
	            int managerId1 = 0; // default

	            if (roleChoice == 1) {
	                role = "EMPLOYEE";

	               
	            } else if (roleChoice == 2) {
	                role = "MANAGER";
	                managerId1 = 0; // managers have no manager above them
	            } else {
	                System.out.println("❌ Invalid choice!");
	                return;
	            }
	            System.out.print("Phone: ");
	            String phone = sc.next();

	            System.out.print("Address: ");
	            sc.nextLine();
	            String address = sc.nextLine();

	            System.out.print("DOB (yyyy-mm-dd): ");
	            Date dobDate = Date.valueOf(sc.next());

	            System.out.print("Joining Date (yyyy-mm-dd): ");
	            Date joiningDate = Date.valueOf(sc.next());

	            System.out.print("Department ID: ");
	            int deptId = sc.nextInt();

	            System.out.print("Designation ID: ");
	            int desigId = sc.nextInt();

	            System.out.print("Manager ID (0 if none): ");
	            int managerId = sc.nextInt();

	            System.out.print("Salary: ");
	            double salary = sc.nextDouble();

	            // ---------------- Validate Department ----------------
	            try (PreparedStatement psDept = con.prepareStatement(
	                    "SELECT COUNT(*) FROM departments WHERE department_id = ?")) {
	                psDept.setInt(1, deptId);
	                ResultSet rsDept = psDept.executeQuery();
	                rsDept.next();
	                if (rsDept.getInt(1) == 0) {
	                    System.out.println("❌ Invalid Department ID!");
	                    con.rollback();
	                    return;
	                }
	            }

	            // ---------------- Validate Designation ----------------
	            try (PreparedStatement psDesig = con.prepareStatement(
	                    "SELECT COUNT(*) FROM designations WHERE designation_id = ?")) {
	                psDesig.setInt(1, desigId);
	                ResultSet rsDesig = psDesig.executeQuery();
	                rsDesig.next();
	                if (rsDesig.getInt(1) == 0) {
	                    System.out.println("❌ Invalid Designation ID!");
	                    con.rollback();
	                    return;
	                }
	            }

	            // ---------------- Validate Manager ----------------
	            if (managerId != 0) {
	                try (PreparedStatement psMgr = con.prepareStatement(
	                        "SELECT COUNT(*) FROM employees WHERE employee_id = ?")) {
	                    psMgr.setInt(1, managerId);
	                    ResultSet rsMgr = psMgr.executeQuery();
	                    rsMgr.next();
	                    if (rsMgr.getInt(1) == 0) {
	                        System.out.println("❌ Invalid Manager ID!");
	                        con.rollback();
	                        return;
	                    }
	                }
	            }
	         // Check if user already exists
	            PreparedStatement psCheckUser = con.prepareStatement(
	                    "SELECT COUNT(*) FROM users WHERE employee_id = ?");
	            psCheckUser.setInt(1, empId);
	            ResultSet rsCheck = psCheckUser.executeQuery();
	            rsCheck.next();
	            if(rsCheck.getInt(1) > 0) {
	                System.out.println("❌ User for this employee already exists!");
	                con.rollback();
	                return;
	            }
	            // ---------------- Insert Employee ----------------
	            String sqlEmp = "INSERT INTO employees " +
	                    "(employee_id, name, email, phone, address, dob, joining_date, " +
	                    "department_id, designation_id, manager_id, salary, status, password, role) " +
	                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	            try (PreparedStatement ps = con.prepareStatement(sqlEmp)) {
	                ps.setInt(1, empId);
	                ps.setString(2, name);
	                ps.setString(3, email);
	                ps.setString(4, phone);
	                ps.setString(5, address);
	                ps.setDate(6, dobDate);
	                ps.setDate(7, joiningDate);
	                ps.setInt(8, deptId);
	                ps.setInt(9, desigId);
	                if (managerId == 0) ps.setNull(10, Types.INTEGER);
	                else ps.setInt(10, managerId);
	                ps.setDouble(11, salary);
	                ps.setString(12, "ACTIVE");
	                ps.setString(13, password); 
	                ps.setString(14, role);
	                int rows = ps.executeUpdate();
	                if (rows == 0) {
	                    System.out.println("❌ Employee not added!");
	                    con.rollback();
	                    return;
	                }
	            }
	           
	            
	         // ---------------- Insert into Users table ----------------
	            String sqlUser = "INSERT INTO users (user_id, employee_id, password, role) " +
	                             "VALUES (USER_SEQ.NEXTVAL, ?, ?, ?)";
	            try (PreparedStatement psUser = con.prepareStatement(sqlUser)) {
	                psUser.setInt(1, empId);
	                psUser.setString(2, password);
	                psUser.setString(3, role);
	                psUser.executeUpdate();
	            }


	            // ---------------- Initialize Leave Balance ----------------
	            String sqlLeaveTypes = "SELECT leave_type_id FROM leave_types";
	            try (PreparedStatement psLeaveTypes = con.prepareStatement(sqlLeaveTypes);
	                 ResultSet rs = psLeaveTypes.executeQuery()) {

	            	String sqlLeave = "INSERT INTO leave_balance (employee_id, leave_type_id, available_days) VALUES (?, ?, ?)";
	            	try (PreparedStatement psLeave = con.prepareStatement(sqlLeave)) {
	            	    while (rs.next()) {
	            	        int leaveTypeId = rs.getInt("leave_type_id");
	            	        psLeave.setInt(1, empId);
	            	        psLeave.setInt(2, leaveTypeId);
	            	        psLeave.setInt(3, 10); // default leave days
	            	        psLeave.addBatch();
	            	    }
	            	    psLeave.executeBatch();
	            	}
	            }

	            con.commit(); // commit all inserts
	            System.out.println("✅ Employee, login, and leave balance created successfully!");
	            logger.info("Admin {} added employee {}", adminId, empId);
	            DBUtil.logAction(adminId, "Added employee " + empId);

	        } catch (SQLIntegrityConstraintViolationException e) {
	            System.out.println("❌ Constraint violation! Check Employee ID, Manager, Department, or Designation.");
	            e.printStackTrace();
	        } catch (SQLException e) {
	            System.out.println("❌ Database error occurred!");
	            e.printStackTrace();
	        }
	    }



	 // ================= VIEW EMPLOYEES =================
	    public static void viewEmployees(int adminId) {
	        logger.info("Admin {} viewing employees", adminId);

	        try (Connection con = DBUtil.getConnection()) {

	            // ---------------- Employees ----------------
	            String sqlEmployees = "SELECT employee_id, name, email, phone, status " +
	                    "FROM employees " +
	                    "WHERE employee_id NOT IN (SELECT DISTINCT manager_id FROM employees WHERE manager_id IS NOT NULL) " +
	                    "ORDER BY employee_id ASC";

	            try (Statement st = con.createStatement();
	                 ResultSet rs = st.executeQuery(sqlEmployees)) {

	                System.out.println("\n========== EMPLOYEE LIST ==========");
	                System.out.printf("%-10s %-15s %-30s %-15s %-10s%n",
	                        "ID", "Name", "Email", "Phone", "Status");
	                System.out.println("---------------------------------------------------------------");

	                boolean found = false;
	                while (rs.next()) {
	                    found = true;
	                    System.out.printf("%-10d %-15s %-30s %-15s %-10s%n",
	                            rs.getInt("employee_id"),
	                            rs.getString("name"),
	                            rs.getString("email"),
	                            rs.getString("phone"),
	                            rs.getString("status"));
	                }

	                if (!found) {
	                    System.out.println("No employees found.");
	                }

	                System.out.println("===================================\n");
	            }

	            // ---------------- Managers ----------------
	            String sqlManagers = "SELECT employee_id, name, email, phone, status " +
	                    "FROM employees " +
	                    "WHERE employee_id IN (SELECT DISTINCT manager_id FROM employees WHERE manager_id IS NOT NULL) " +
	                    "ORDER BY employee_id ASC";

	            try (Statement st = con.createStatement();
	                 ResultSet rs = st.executeQuery(sqlManagers)) {

	                System.out.println("\n========== MANAGER LIST ==========");
	                System.out.printf("%-10s %-15s %-30s %-15s %-10s%n",
	                        "ID", "Name", "Email", "Phone", "Status");
	                System.out.println("---------------------------------------------------------------");

	                boolean found = false;
	                while (rs.next()) {
	                    found = true;
	                    System.out.printf("%-10d %-15s %-30s %-15s %-10s%n",
	                            rs.getInt("employee_id"),
	                            rs.getString("name"),
	                            rs.getString("email"),
	                            rs.getString("phone"),
	                            rs.getString("status"));
	                }

	                if (!found) {
	                    System.out.println("No managers found.");
	                }

	                System.out.println("===================================\n");
	            }

	            DBUtil.logAction(adminId, "Viewed employees and managers");

	        } catch (SQLException e) {
	            System.out.println("❌ Error while fetching employees/managers!");
	            logger.error("Error while viewing employees/managers", e);
	        }
	    }


	 // ================= UPDATE EMPLOYEE STATUS =================
	    public static void updateEmployeeStatus(int adminId) {

	        Scanner sc = new Scanner(System.in);

	        System.out.print("Employee ID: ");
	        int empId = sc.nextInt();

	        sc.nextLine(); // clear buffer

	        System.out.print("Enter Status (ACTIVE / INACTIVE): ");
	        String status = sc.nextLine().trim().toUpperCase();

	        // ✅ Validate input before DB call
	        if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
	            System.out.println("❌ Invalid status! Please enter ACTIVE or INACTIVE only.");
	            return;
	        }

	        logger.info("Admin {} updating status of employee {}", adminId, empId);

	        String sql = "UPDATE employees SET status = ? WHERE employee_id = ?";

	        try (Connection con = DBUtil.getConnection();
	             PreparedStatement ps = con.prepareStatement(sql)) {

	            ps.setString(1, status);
	            ps.setInt(2, empId);

	            int rows = ps.executeUpdate();

	            if (rows > 0) {
	                System.out.println("✅ Employee status updated successfully!");
	                System.out.println("➡ Employee ID: " + empId);
	                System.out.println("➡ New Status: " + status);

	                DBUtil.logAction(adminId,
	                        "Updated employee " + empId + " status to " + status);

	                logger.info("Employee {} status updated to {}", empId, status);
	            } else {
	                System.out.println("❌ Employee not found!");
	                logger.warn("Employee {} not found while updating status", empId);
	            }

	        } catch (Exception e) {
	            System.out.println("❌ Failed to update employee status!");
	            logger.error("Error updating employee status", e);
	        }
	    }


	 // ================= ASSIGN MANAGER =================
	    public static void assignManager(int adminId) {

	        Scanner sc = new Scanner(System.in);

	        System.out.print("Employee ID: ");
	        int empId = sc.nextInt();

	        System.out.print("Manager ID: ");
	        int managerId = sc.nextInt();

	        logger.info("Admin {} assigning manager {} to employee {}",
	                adminId, managerId, empId);

	        try (Connection con = DBUtil.getConnection()) {

	            // ✅ Get Manager Name
	            PreparedStatement mgrPs =
	                    con.prepareStatement(
	                            "SELECT name FROM employees WHERE employee_id=?");
	            mgrPs.setInt(1, managerId);

	            ResultSet mgrRs = mgrPs.executeQuery();

	            if (!mgrRs.next()) {
	                System.out.println("❌ Manager ID not found");
	                return;
	            }

	            String managerName = mgrRs.getString("name");

	            // ✅ Update employee with manager
	            PreparedStatement ps =
	                    con.prepareStatement(
	                            "UPDATE employees SET manager_id=? WHERE employee_id=?");

	            ps.setInt(1, managerId);
	            ps.setInt(2, empId);

	            int rows = ps.executeUpdate();

	            if (rows > 0) {
	                DBUtil.logAction(adminId,
	                        "Assigned manager " + managerName + " to employee " + empId);

	                System.out.println(
	                        "✅ Manager " + managerName + " assigned successfully!");

	                logger.info("Manager {} assigned to employee {}", managerName, empId);
	            } else {
	                System.out.println("❌ Employee ID not found");
	            }

	        } catch (Exception e) {
	            logger.error("Error assigning manager", e);
	            System.out.println("❌ Error while assigning manager");
	        }
	    }


	 // ================= LEAVE REPORTS =================
	    public static void viewLeaveReports(int adminId) {

	        logger.info("Admin {} viewing leave reports", adminId);

	        try (Connection con = DBUtil.getConnection()) {

	            String sql =
	                    "SELECT e.employee_id, e.name, lt.leave_name, " +
	                    "lr.start_date, lr.end_date, lr.status " +
	                    "FROM leave_requests lr " +
	                    "JOIN employees e ON lr.employee_id = e.employee_id " +
	                    "JOIN leave_types lt ON lr.leave_type_id = lt.leave_type_id " +
	                    "ORDER BY lr.start_date DESC";

	            ResultSet rs = con.createStatement().executeQuery(sql);

	            System.out.println("\n===== LEAVE REPORTS =====");
	            System.out.printf(
	                    "%-6s %-15s %-15s %-12s %-12s %-10s%n",
	                    "ID", "Employee", "Leave Type", "From", "To", "Status");
	            System.out.println("---------------------------------------------------------------------");

	            boolean found = false;

	            while (rs.next()) {
	                found = true;
	                System.out.printf(  "%-6d %-15s %-15s %-12s %-12s %-10s%n",
	                        rs.getInt("employee_id"),
	                        rs.getString("name"),
	                        rs.getString("leave_name"),
	                        rs.getDate("start_date"),
	                        rs.getDate("end_date"),
	                        rs.getString("status")
	                );
	            }

	            if (!found) {
	                System.out.println("No leave records found.");
	            }

	            DBUtil.logAction(adminId, "Viewed leave reports");

	        } catch (Exception e) {
	            logger.error("Error viewing leave reports", e);
	            System.out.println("❌ Error while fetching leave reports");
	        }
	    }



	 
	 // ================= ADD HOLIDAY =================
	    public static void addHoliday(int adminId) {
	        Scanner sc = new Scanner(System.in);

	        System.out.print("Holiday Date (yyyy-mm-dd): ");
	        String dateStr = sc.nextLine();

	        System.out.print("Description: ");
	        String desc = sc.nextLine();

	        try (Connection con = DBUtil.getConnection()) {

	            // Convert string to java.sql.Date
	            java.sql.Date holidayDate = java.sql.Date.valueOf(dateStr);

	            // Check if holiday for this date already exists
	            PreparedStatement check = con.prepareStatement(
	                "SELECT COUNT(*) FROM holidays WHERE holiday_date = ?");
	            check.setDate(1, holidayDate);
	            ResultSet rs = check.executeQuery();
	            rs.next();
	            if (rs.getInt(1) > 0) {
	                System.out.println("❌ Holiday for this date already exists!");
	                return;
	            }

	            // Insert new holiday using sequence (assumes sequence 'holiday_seq' exists)
	            String sql = "INSERT INTO holidays (holiday_id, holiday_date, description) " +
	                         "VALUES (holiday_seq.NEXTVAL, ?, ?)";
	            PreparedStatement ps = con.prepareStatement(sql);
	            ps.setDate(1, holidayDate);
	            ps.setString(2, desc);
	            ps.executeUpdate();

	            DBUtil.logAction(adminId, "Added holiday on " + dateStr);
	            System.out.println("✅ Holiday added successfully!");

	        } catch (IllegalArgumentException e) {
	            System.out.println("❌ Invalid date format! Use yyyy-mm-dd.");
	        } catch (Exception e) {
	            System.out.println("❌ Error while adding holiday");
	            e.printStackTrace();
	        }
	    }

	 // ================= ADD DEPARTMENT =================
	    public static void addDepartment(int adminId) {
	        Scanner sc = new Scanner(System.in);

	        System.out.print("Department Name: ");
	        String name = sc.nextLine().trim();

	        if (name.isEmpty()) {
	            System.out.println("❌ Department name cannot be empty!");
	            return;
	        }

	        try (Connection con = DBUtil.getConnection()) {

	            // Check if department already exists
	            PreparedStatement check = con.prepareStatement(
	                    "SELECT COUNT(*) FROM departments WHERE LOWER(department_name) = LOWER(?)");
	            check.setString(1, name);
	            ResultSet rs = check.executeQuery();
	            rs.next();
	            if (rs.getInt(1) > 0) {
	                System.out.println("❌ Department already exists!");
	                return;
	            }

	            // Sync sequence with current max ID to avoid ORA-00001
	            Statement seqStmt = con.createStatement();
	            seqStmt.execute(
	                    "BEGIN " +
	                    "EXECUTE IMMEDIATE 'ALTER SEQUENCE department_seq INCREMENT BY 1'; " +
	                    "EXECUTE IMMEDIATE 'SELECT MAX(department_id) + 1 INTO :nextval FROM departments'; " +
	                    "EXECUTE IMMEDIATE 'ALTER SEQUENCE department_seq INCREMENT BY 1'; " +
	                    "END;"
	            );

	            // Insert new department
	            String sql = "INSERT INTO departments (department_id, department_name) " +
	                         "VALUES (department_seq.NEXTVAL, ?)";
	            PreparedStatement ps = con.prepareStatement(sql);
	            ps.setString(1, name);
	            ps.executeUpdate();

	            DBUtil.logAction(adminId, "Added department " + name);
	            System.out.println("✅ Department added successfully!");

	        } catch (Exception e) {
	            System.out.println("❌ Error adding department");
	            e.printStackTrace();
	        }
	    }



	 // ================= ADD DESIGNATION =================
	    public static void addDesignation(int adminId) {
	        Scanner sc = new Scanner(System.in);
	        System.out.print("Designation Name: ");
	        String name = sc.nextLine().trim();

	        if (name.isEmpty()) {
	            System.out.println("❌ Designation name cannot be empty!");
	            return;
	        }

	        try (Connection con = DBUtil.getConnection()) {

	            // Check if designation already exists (ignore case & trim)
	            String checkSQL = "SELECT COUNT(*) FROM designations WHERE LOWER(TRIM(designation_name)) = LOWER(TRIM(?))";
	            PreparedStatement check = con.prepareStatement(checkSQL);
	            check.setString(1, name);
	            ResultSet rs = check.executeQuery();
	            rs.next();
	            if (rs.getInt(1) > 0) {
	                System.out.println("❌ Designation already exists!");
	                return;
	            }

	            // Insert new designation using sequence
	            String insertSQL = "INSERT INTO designations (designation_id, designation_name) VALUES (designation_seq.NEXTVAL, ?)";
	            PreparedStatement ps = con.prepareStatement(insertSQL);
	            ps.setString(1, name);
	            int rows = ps.executeUpdate();

	            if (rows > 0) {
	                DBUtil.logAction(adminId, "Added designation " + name);
	                System.out.println("✅ Designation added successfully!");
	            }

	        } catch (SQLSyntaxErrorException e) {
	            System.out.println("❌ Sequence 'designation_seq' does not exist! Please create it in the database.");
	            e.printStackTrace();
	        } catch (SQLIntegrityConstraintViolationException e) {
	            System.out.println("❌ Designation already exists (duplicate prevented by DB)!");
	        } catch (Exception e) {
	            System.out.println("❌ Error adding designation");
	            e.printStackTrace();
	        }
	    }


	 // ================= PERFORMANCE REVIEWS =================
	    public static void viewAllPerformanceReviews(int adminId) {

	        logger.info("Admin {} viewing performance reviews", adminId);

	        try (Connection con = DBUtil.getConnection()) {

	            String sql =
	                    "SELECT e.employee_id, e.name, pr.year, " +
	                    "pr.self_rating, pr.manager_rating, pr.status " +
	                    "FROM performance_reviews pr " +
	                    "JOIN employees e ON pr.employee_id = e.employee_id " +
	                    "ORDER BY pr.year DESC";

	            ResultSet rs = con.createStatement().executeQuery(sql);

	            System.out.println("\n===== PERFORMANCE REVIEWS =====");
	            System.out.printf("%-6s %-15s %-6s %-12s %-15s %-10s%n",
	                    "ID", "Name", "Year", "Self Rating", "Manager Rating", "Status");
	            System.out.println("---------------------------------------------------------------");

	            boolean found = false;

	            while (rs.next()) {
	                found = true;
	                System.out.printf("%-6d %-15s %-6d %-12d %-15d %-10s%n",
	                        rs.getInt("employee_id"),
	                        rs.getString("name"),
	                        rs.getInt("year"),
	                        rs.getInt("self_rating"),
	                        rs.getInt("manager_rating"),
	                        rs.getString("status"));
	            }

	            if (!found) {
	                System.out.println("⚠️ No performance reviews found.");
	            }

	            DBUtil.logAction(adminId, "Viewed performance reviews");
	            logger.info("Performance reviews viewed successfully by admin {}", adminId);

	        } catch (Exception e) {
	            System.out.println("❌ Error fetching performance reviews");
	            logger.error("Error viewing performance reviews", e);
	        }
	    }

	 // ================= ANNOUNCEMENT =================
	    public static void addAnnouncement(int adminId) {
	        Scanner sc = new Scanner(System.in);

	        System.out.println("\n===== PUBLISH ANNOUNCEMENT =====");

	        System.out.print("Title: ");
	        String title = sc.nextLine().trim();

	        System.out.print("Message: ");
	        String message = sc.nextLine().trim();

	        if (title.isEmpty() || message.isEmpty()) {
	            System.out.println("❌ Title and Message cannot be empty!");
	            return;
	        }

	        logger.info("Admin {} publishing announcement {}", adminId, title);

	        try (Connection con = DBUtil.getConnection()) {

	            // Insert with sequence and created_at
	            String sql = "INSERT INTO announcements (announcement_id, title, message, created_at) " +
	                         "VALUES (announcement_seq.NEXTVAL, ?, ?, SYSDATE)";

	            PreparedStatement ps = con.prepareStatement(sql);
	            ps.setString(1, title);
	            ps.setString(2, message);

	            int rows = ps.executeUpdate();

	            if (rows > 0) {
	                System.out.println("✅ Announcement published successfully!");
	                DBUtil.logAction(adminId, "Published announcement: " + title);
	            } else {
	                System.out.println("⚠️ Announcement not published.");
	            }

	        } catch (Exception e) {
	            System.out.println("❌ Error while publishing announcement.");
	            logger.error("Error publishing announcement", e);
	            e.printStackTrace();
	        }
	    }



	 // ================= NOTIFICATION =================
	    public static void sendNotification(int adminId) {
	        Scanner sc = new Scanner(System.in);

	        System.out.println("\n===== SEND NOTIFICATION =====");

	        System.out.print("Employee ID: ");
	        int empId = sc.nextInt();
	        sc.nextLine(); // consume newline

	        System.out.print("Message: ");
	        String msg = sc.nextLine().trim();

	        System.out.print("Type (LEAVE / PERFORMANCE / ANNOUNCEMENT): ");
	        String type = sc.next().toUpperCase().trim();

	        // Validation
	        if (msg.isEmpty()) {
	            System.out.println("❌ Message cannot be empty!");
	            return;
	        }

	        if (!type.equals("LEAVE") && !type.equals("PERFORMANCE") && !type.equals("ANNOUNCEMENT")) {
	            System.out.println("❌ Invalid notification type!");
	            return;
	        }

	        logger.info("Admin {} sending notification to employee {}", adminId, empId);

	        try (Connection con = DBUtil.getConnection()) {

	            // Optional: Check if employee exists
	            String checkEmp = "SELECT COUNT(*) FROM employees WHERE employee_id = ?";
	            try (PreparedStatement psCheck = con.prepareStatement(checkEmp)) {
	                psCheck.setInt(1, empId);
	                ResultSet rs = psCheck.executeQuery();
	                rs.next();
	                if (rs.getInt(1) == 0) {
	                    System.out.println("❌ Employee ID does not exist!");
	                    return;
	                }
	            }

	            // Insert notification using sequence
	            String sql = "INSERT INTO notifications (notification_id, employee_id, message, type, created_at) " +
	                         "VALUES (notification_seq.NEXTVAL, ?, ?, ?, SYSDATE)";

	            try (PreparedStatement ps = con.prepareStatement(sql)) {
	                ps.setInt(1, empId);
	                ps.setString(2, msg);
	                ps.setString(3, type);

	                int rows = ps.executeUpdate();
	                if (rows > 0) {
	                    System.out.println("✅ Notification sent successfully!");
	                    DBUtil.logAction(adminId, "Sent " + type + " notification to employee " + empId);
	                } else {
	                    System.out.println("⚠️ Notification not sent.");
	                }
	            }

	        } catch (SQLIntegrityConstraintViolationException e) {
	            System.out.println("❌ Constraint violation: Notification ID or employee may be invalid.");
	            logger.error("Constraint violation while sending notification", e);

	        } catch (SQLException e) {
	            System.out.println("❌ Error while sending notification.");
	            logger.error("Error sending notification", e);
	            e.printStackTrace();
	        }
	    }



	 // ================= AUDIT LOGS =================
	    public static void viewAuditLogs(int adminId) {

	        System.out.println("\n===== AUDIT LOGS =====");

	        logger.info("Admin {} viewing audit logs", adminId);

	        try (Connection con = DBUtil.getConnection()) {

	            String sql =
	                    "SELECT user_id, action, log_time " +
	                    "FROM audit_logs " +
	                    "ORDER BY log_time DESC";

	            ResultSet rs = con.createStatement().executeQuery(sql);

	            boolean hasData = false;

	            System.out.printf("%-10s %-45s %-25s%n",
	                    "USER ID", "ACTION", "LOG TIME");
	            System.out.println("---------------------------------------------------------------");

	            while (rs.next()) {
	                hasData = true;
	                System.out.printf("%-10d %-45s %-25s%n",
	                        rs.getInt("user_id"),
	                        rs.getString("action"),
	                        rs.getTimestamp("log_time"));
	            }

	            if (!hasData) {
	                System.out.println("⚠️ No audit logs found.");
	            }

	            DBUtil.logAction(adminId, "Viewed audit logs");

	        } catch (Exception e) {
	            System.out.println("❌ Error while fetching audit logs.");
	            logger.error("Error viewing audit logs", e);
	        }
	    }

	    }

	    

	    
	

