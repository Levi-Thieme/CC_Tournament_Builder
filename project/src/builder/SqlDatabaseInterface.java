package builder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Package Level Security
 * 
 * @author Team 5
 *
 */
public class SqlDatabaseInterface {
	
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost/tournament_builder";
	static final String TOURNAMENT_TABLE_NAME = "tournaments";

	//  Database credentials
	static final String USER = "admin";
	static final String PASS = "admin";
	
	/** Connects to the SQL database and return the active connection
	 * 
	 * @return active connection or null if unconnected
	 */
	static Connection establishConnection() {
		
		Connection conn = null;
		
		try {

			// Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return conn;
	}
	
	/** Runs the Query string and returns the ResultSet
	 * 
	 * @param sqlStmt - String query to run
	 * @return ResultSet of the Query or Null if unsuccessful
	 */
	static ResultSet runQuery(String sqlStmt, Connection conn) {
		
		Statement stmt = null;	// Statement
		ResultSet rs = null;	// Result set

		try {

			// Create the statement
			stmt = conn.createStatement();

			// Execute the statement
			rs = stmt.executeQuery(sqlStmt);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Return ResultSet
		return rs;
	}
	
	/** Runs an SQL query without expecting a ResultSet **
	 * 
	 * FOR INSERT, UPDATE, or DELETE
	 * 
	 * @param sqlStmt - statement to execute
	 * @return true if successful
	 */
	public static boolean runVoidQuery(String sqlStmt, Connection conn) {

		boolean success = false;

		Statement stmt = null;

		try {

			// Create the statement
			stmt = conn.createStatement();

			// Execute the statement
			stmt.executeUpdate(sqlStmt);

			// Indicate success
			success = true;

		} catch (SQLException e) {
			e.printStackTrace();
			success = false;
		} finally {
			// Close leaks
			close(stmt);
		}

		
		return success;

	}
	
	public static boolean runVoidQuery(PreparedStatement stmt, Connection conn) {
		
		boolean success = false;

		try {

			// Execute the statement
			stmt.executeUpdate();

			// Indicate success
			success = true;

		} catch (SQLException e) {
			e.printStackTrace();
			success = false;
		} finally {
			// Close leaks
			close(stmt);
		}

		
		return success;
	}
	
	/** Private helper class for closing a
	 *  ResultSet safely
	 *  
	 * @param rs - Result Set to close - may be null
	 */
	static void close(ResultSet rs) {
		
		// Attempt to close any statements attached to Result Set
		try {
			if(rs != null) {
				// Recursive clean up if necessary
				if(rs.getStatement() != null) {
					try {
						rs.getStatement().close();
					} catch(SQLException e) {
						e.printStackTrace(); 
					}
				}
					
				// Close the result statement
				if(rs != null) {
					rs.close();
				}
			}
				
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	static void close(Connection conn) {
		// Clean-up environment
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/** Private helper class for closing a
	 *  statement safely
	 *  
	 * @param stmt - Statement to close - may be null
	 */
	private static void close(Statement stmt) {
		
		// Attempt to close statement
		try {
			if(stmt != null){
				stmt.close();
			}	
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
