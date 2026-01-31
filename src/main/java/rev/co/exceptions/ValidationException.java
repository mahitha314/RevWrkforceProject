package rev.co.exceptions;

public class ValidationException extends Exception {
	

	// ================== Validation Exceptions ==================
	public class ValidationException1 extends Exception {
	    public ValidationException1(String message) {
	        super(message);
	    }
	}

	// ================== Duplicate Entry Exceptions ==================
	public class DuplicateEntryException extends Exception {
	    public DuplicateEntryException(String message) {
	        super(message);
	    }
	}

	// ================== Database Constraint Exceptions ==================
	public class DatabaseConstraintException extends Exception {
	    public DatabaseConstraintException(String message) {
	        super(message);
	    }
	}

	// ================== Invalid ID Exceptions ==================
	public class InvalidIdException extends Exception {
	    public InvalidIdException(String message) {
	        super(message);
	    }
	}

	// ================== Date Format Exceptions ==================
	public class DateFormatException extends Exception {
	    public DateFormatException(String message) {
	        super(message);
	    }
	


	}}
