/* Setup for hw5
 * assumes twp stored procedures (functions) are separately installed in
 * your Oracle account--see assign_pilot in createSP1.sql.
 */
import java.io.IOException;
import java.util.Scanner;
import java.sql.*;

public class AssignFlights1SP {
	private static Connection connection = null;

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		// Prompt the user for connect information
		String username = null;
		String password = null;
		String connStr = null;
		try {
				username = readEntry(in, "Oracle username: ");
				password = readEntry(in, "Oracle password: ");
				String host = readEntry(in, "host: ");
				String port = readEntry(in, "port (often 1521): ");
				String sid = readEntry(in, "sid (site id): ");
				connStr = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
		} catch (IOException e) {
			System.out.println("Problem with user input, please try again\n");
			System.exit(3);
		}
		System.out.println("using connection string: " + connStr);
		System.out.print("Connecting to the database...");

		try {
			connection = getConnected(connStr, username, password);
		} catch (SQLException except) {
			System.out.println("Problem with JDBC Connection");
			System.out.println(except.getMessage());
			System.exit(1);
		}
		try {
			// reinitialize (harmless if first time)
			Statement stmt = connection.createStatement();

			stmt.execute("delete from flight_assignments");
			stmt.execute("delete from delayed_flights");
			stmt.execute("delete from new_location");
			stmt.close();
		} catch (SQLException except) {
			System.out.println("Problem with initialization");
			System.out.println(except.getMessage());
			System.exit(1);
		}

		// reads the flights1 table in order of departure time
		PreparedStatement pstmt_1 = null;
		String query_1 = "SELECT *  FROM flights1  ORDER BY departs";

		try {
			pstmt_1 = connection.prepareStatement(query_1);
			pstmt_1.clearParameters();
			ResultSet rs_1 = pstmt_1.executeQuery();
		    // as in AssignFlights1, scan flights1
			while (rs_1.next()){ // loop over rows of flights...
				int flno = rs_1.getInt("flno"); // Retrieve the column value
				String origin = rs_1.getString("origin");
				String destination = rs_1.getString("destination");
				int distance = rs_1.getInt("distance");
				Timestamp departs = rs_1.getTimestamp("departs");
				Timestamp arrives = rs_1.getTimestamp("arrives");
				double price = rs_1.getFloat("price");
				int isProcessed = rs_1.getInt("is_processed");
				Flight flight = new Flight(flno, origin, destination, distance, departs, arrives, price, isProcessed); // store

				// find an airplane and a certified pilot for each flight
				assignFlight(flight);
			}
			findFinalLocations();

		} catch (SQLException except) {
			System.out.println("Problem with main loop");
			System.out.println(except);
			printSQLException(except);

		} finally {
			// Close the PreparedStatement & its ResultSet (if they exist)
			closePreparedStatement(pstmt_1);
		}
	}

	private static void assignFlight(Flight flight) throws SQLException {
		CallableStatement cstmt = null;
		try {
			// Prepare to call the stored procedure
			System.out.println("assignFlight for "+flight.getFlightNumber());
			cstmt = connection.prepareCall("{call assign_pilot (?, ?, ?)}");
			cstmt.setInt(1, flight.getFlightNumber());
			cstmt.setInt(2, flight.getDistance());
			cstmt.setString(3, flight.getOrigin());
			// Choose the pilot in PL/SQL, store results in tables
			cstmt.execute();
		} catch (SQLException e) {
			System.out.println("Problem with assignFlight ");
			throw (e); // let caller handle after above output
		} finally {
			closeCallableStatement(cstmt);
		}
	}

	private static void findFinalLocations() throws SQLException {
		// as in AssignFlights1.java
		PreparedStatement pstmt_5 = null;
		PreparedStatement pstmt_6 = null;
		// join the flight_assignments table and flights1 table, and then
		// fill in the new_location table
		String query_5 = "SELECT FA.eid as eid, F.destination, F.arrives "
				+ "FROM  flight_assignments FA, flights1 F " // emps that flew
				+ "WHERE FA.flno = F.flno "
				+ " UNION " // emps that never flew
				+ "SELECT s.eid as eid, s.city, null from start_location s"
				+ " where s.eid not in (select f.eid from flight_assignments f)" + " ORDER by eid";
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
				System.out.println("one row inserted into new_location");
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

	public static void closePreparedStatement(Statement ps) {
		try {
			if (ps != null) {
				ps.close();
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

	public static void closeCallableStatement(CallableStatement cstmt) {
		try {
			if (cstmt != null) {
				cstmt.close();
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
