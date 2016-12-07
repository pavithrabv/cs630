/**
 * AssignFlights2, a JDBC program for Homework 4 of cs430/630 Fall 2016
 * Reads tables flights1, aircraft1, certified1, and start_location to
 * find employees (pilots) certified for aircraft that have cruising range
 * sufficient to fly as directed in flights1 to the destination in one hop.
 * The pilots need to be at the origin city, as recorded in start_location. 
 * The program fills tables flight_assignmants and delayed_flights with
 * successful assignments and flights delayed because no pilot is available,
 * respectively. Table new_location is filled to record where the pilots end up.
 * 
 * This version of the program is written without using a cursor across flights1,
 * to handle the more dynamic case in which flights could be added to flights1
 * while this program is running. The assignFlight() method now finds an
 * unprocessed flight in the flights1 table and does the work for it.
 * An additional column "is_processed" in flights1 records whether or not
 * an individual flight has been processed.
 * After the flights are processed this way, the new_location table is filled.
 * 
 * The program produces more output than a production program would, to aid
 * in its understanding. Student solutions may produce much less output.
 * The official results of this program are in tables assigned_flights,
 * delayed_flights, and new_location.
 */

import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class AssignFlights2SP {
	private static Connection connection = null;

	public static void main(String args[]) {
		String dbSys = null;
		String username = null;
		String password = null;
		String connStr = null;
		Scanner in = null;
		try {
			in = new Scanner(System.in);
			System.out.println("Please enter information for connection to the database");
			dbSys = readEntry(in, "Using Oracle (o) or MySql (m)? ");
			// Prompt the user for connect information
			if (dbSys.equals("o")) {
				username = readEntry(in, "Oracle username: ");
				password = readEntry(in, "Oracle password: ");
				String host = readEntry(in, "host: ");
				String port = readEntry(in, "port (often 1521): ");
				String sid = readEntry(in, "sid (site id): ");
				connStr = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
			} else if (dbSys.equals("m")) {// MySQL--
				username = readEntry(in, "MySQL username: ");
				password = readEntry(in, "MySQL password: ");
				String host = readEntry(in, "host: ");
				String port = readEntry(in, "port (often 3306): ");
				String db = username + "db";
				connStr = "jdbc:mysql://" + host + ":" + port + "/" + db;
			}
		} catch (IOException e) {
			System.out.println("Problem with user input, please try again\n");
			System.exit(1);
		}
		System.out.println("using connection string: " + connStr);
		System.out.print("Connecting to the database...");

		try {
			connection = getConnected(connStr, username, password);
			connection.setAutoCommit(false);
		} catch (SQLException except) {
			System.out.println("Problem with JDBC Connection");
			System.out.println(except.getMessage());
			System.exit(2);
		} 

		Statement stmt = null;
		try {
			// Create a statement
			stmt = connection.createStatement();

			stmt.execute("delete from flight_assignments");
			stmt.execute("delete from delayed_flights");
			stmt.execute("delete from new_location");
			stmt.execute("update flights1 set is_processed = 0");
			connection.commit();
		} catch (SQLException except) {
			System.out.println("Problem with initialization");
			System.out.println(except.getMessage());
			System.exit(3);
		} finally {
			closeStatement(stmt);

		}

		try {
			connection.setAutoCommit(false); // take over transaction lifetime
			//connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

			boolean done = false;

			assignFlight();
			findFinalLocations();
			
		} catch (SQLException except) {
			System.out.println("Problem handled in main");
			printSQLException(except);			
		} finally {
			closeConnection(connection);  // not really necessary, since we are exiting now
		}
	}

	private static boolean assignFlight() throws SQLException {
		CallableStatement cstmt = null;


		try {
			cstmt = connection.prepareCall("{call assign_pilot2");
			cstmt.execute();
		} catch (SQLException e) {
			System.out.println("Problem with assignFlight ");
			throw (e); // let caller handle after above output
		} finally {
			closeCallableStatement(cstmt);
		}
		return false;

	}

	private static void findFinalLocations() throws SQLException {

		PreparedStatement pstmt_5 = null;
		PreparedStatement pstmt_6 = null;
		// join the flight_assignments table and flights1 table, and then
		// fill in the new_location table
		// This could be done in two queries, since the groups are disjoint
		String query_5 = "SELECT FA.eid as eid, F.destination, F.arrives "
		+ "FROM  flight_assignments FA, flights1 F "  // emps taking flights to new locs
				+ "WHERE FA.flno = F.flno "
		   + " UNION "                             // emps that never flew
		   + "SELECT s.eid as eid, s.city, null from start_location s"
				+ " where s.eid not in (select f.eid from flight_assignments f)"
		   + " ORDER by eid";
		try {
			pstmt_5 = connection.prepareStatement(query_5);
			pstmt_5.clearParameters();
			ResultSet rs_5 = pstmt_5.executeQuery();
			while (rs_5.next()) {
				int eid = rs_5.getInt("eid");
				String destination = rs_5.getString("destination");
				Timestamp arrives = rs_5.getTimestamp("arrives");
				
				String query_6 = "INSERT INTO new_location (eid, city, arrival_time)  " + "VALUES (?, ?, ?)";
				pstmt_6 = connection.prepareStatement(query_6);
				pstmt_6.setInt(1, eid);
				pstmt_6.setString(2, destination);
				pstmt_6.setTimestamp(3, arrives);

				pstmt_6.executeUpdate();
			}
		} catch (SQLException e) {
			System.out.println("Problem with findFinalLocations ");
			throw (e); // let caller handle after above output
		} finally {
			// Close the PreparedStatement & its ResultSet (if they exist)
			closeStatement(pstmt_5);
			closeStatement(pstmt_6);
		}
	}
	public static void closeStatement(Statement ps) {
		try {
			if (ps != null) {
				ps.close();
			}
		} catch (SQLException e) {
			System.out.println(e);
		}
	}

	public static void closeCallableStatement(CallableStatement cstmt) {
		try {
			if (cstmt != null) {
				cstmt.close();
			}
		} catch (SQLException e) {
			System.out.println(e);
		}
	}

	public static void closeResultSet(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			System.out.println(e);
		}
	}
	public static Connection getConnected(String connStr, String user, String password) throws SQLException {

		System.out.println("using connection string: " + connStr);
		System.out.print("Connecting to the database...");
		System.out.flush();

		// Connect to the database
		Connection conn = DriverManager.getConnection(connStr, user, password);
		System.out.println("connected.");
		return conn;
	}

	public static void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close(); // this also closes the Statement and
								// ResultSet, if any
			} catch (SQLException e) {
				System.out.println("Problem with closing JDBC Connection\n");
				printSQLException(e);
			}
		}
	}

	// print out all exceptions connected to e by nextException or getCause
	static void printSQLException(SQLException e) {
		// SQLExceptions can be delivered in lists (e.getNextException)
		// Each such exception can have a cause (e.getCause, from Throwable)
		while (e != null) {
			System.out.println("SQLException Message:" + e.getMessage());
			Throwable t = e.getCause();
			while (t != null) {
				System.out.println("SQLException Cause:" + t);
				t = t.getCause();
			}
			e = e.getNextException();
		}
	}

	// super-simple prompted input from user
	public static String readEntry(Scanner in, String prompt) throws IOException {
		System.out.print(prompt);
		return in.nextLine().trim();
	}

}
