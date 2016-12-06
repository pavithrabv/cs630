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
		} catch (SQLException except) {
			System.out.println("Problem with initialization");
			System.out.println(except.getMessage());
			System.exit(3);
		} finally {
			closeStatement(stmt);
		}

		try {
			connection.setAutoCommit(false); // take over transaction lifetime
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

			boolean done = false;
			// process by each row of flights table
			while (!done) {																															// the
				// find an airplane and a certified pilot for each flight
				done = assignFlight();
			}
			findFinalLocations();
			
		} catch (SQLException except) {
			System.out.println("Problem handled in main");
			printSQLException(except);			
		} finally {
			closeConnection(connection);  // not really necessary, since we are exiting now
		}
	}

	private static boolean assignFlight() throws SQLException {
		Flight flight = null;
		PreparedStatement pstmt_1 = null, pstmt_2 = null, pstmt_3 = null, pstmt_4 = null, pstmt_5 = null;
		try {
			String query_1 = "SELECT * from flights1 f "
					 + "WHERE f.is_processed = 0"
					+ " ORDER BY departs";
			pstmt_1 = connection.prepareStatement(query_1);
			ResultSet rs_1 = pstmt_1.executeQuery();
			if (rs_1.next()) {
				int flno = rs_1.getInt("flno"); // Retrieve the column value
				String origin = rs_1.getString("origin");
				String destination = rs_1.getString("destination");
				int distance = rs_1.getInt("distance");
				Timestamp departs = rs_1.getTimestamp("departs");
				Timestamp arrives = rs_1.getTimestamp("arrives");
				double price = rs_1.getFloat("price");
				int isProcessed = rs_1.getInt("is_processed");
				// Note: the following is not required. Could just use the indiv. variables
				flight = new Flight(flno, origin, destination, distance, departs, arrives, price, isProcessed); // store
			} else {
				System.out.println("no more rows");
				closeStatement(pstmt_1);// Close the PreparedStatement & its ResultSet
				return true;  // done: no more flights to process
			}
		} finally {
			closeStatement(pstmt_1);
		}
		int aid = -1, eid = -1;  // in case we can't find a good eid, aid
		try {
			String query_2 = "SELECT A.aid, A.aname, E.eid, A.cruisingrange   "
					+ " FROM aircraft1 A, certified1 E, start_location sl  "
					+ " WHERE (E.aid = A.aid and sl.eid = e.eid) AND A.cruisingrange > ? "
					+ " and E.eid not in (select fa.eid from flight_assignments fa) " + " and sl.city = ?"
					+ " ORDER BY A.cruisingrange";
			pstmt_2 = connection.prepareStatement(query_2);
			pstmt_2.clearParameters();
			pstmt_2.setInt(1, flight.getDistance()); // feed the value to "?"
			pstmt_2.setString(2, flight.getOrigin());
			String origin = flight.getOrigin();
			ResultSet rs_2 = pstmt_2.executeQuery();
			if (rs_2.next()) {
				aid = rs_2.getInt("aid");
				eid = rs_2.getInt("eid");
				int cruisingrange = +rs_2.getInt("cruisingrange");
				System.out.println("for flt " + flight.getFlightNumber() + ", from " + origin + ", found: " + aid + " "
						+ eid + " (" + cruisingrange + ")");
				while (rs_2.next()) {  // this loop is unnecessary, but shows the other possibilities
					int aid2 = rs_2.getInt("aid");  // at this point
					int eid2 = rs_2.getInt("eid");
					int cruisingrange2 = rs_2.getInt("cruisingrange");
					if (cruisingrange2 > cruisingrange)
						break; // not a tie
					System.out.println("  extra info: found tie:" + aid2 + " " + eid2 + " (" + cruisingrange2 + ")");
				}
			}
		} finally {
			closeStatement(pstmt_2);
		}
		try {
			// OK, found eid and aid for flight or -1 for both, check it off--
			String query_3 = "UPDATE flights1 SET is_processed = 1 WHERE flno = ?";
			pstmt_3 = connection.prepareStatement(query_3);
			pstmt_3.setInt(1, flight.getFlightNumber());
			pstmt_3.executeUpdate();
			pstmt_3.close();
			System.out.println("flights1 row updated");
			// Inserts a new row in flight_assignments or delayed_flights
			if (eid > 0)  // found good eid, aid
			{
				String query_4 = "INSERT INTO flight_assignments (flno, aid, eid)  " + "VALUES (?, ?, ?)";
				pstmt_4 = connection.prepareStatement(query_4);
				pstmt_4.setInt(1, flight.getFlightNumber());
				pstmt_4.setInt(2, aid);
				pstmt_4.setInt(3, eid);
				pstmt_4.executeUpdate();
				pstmt_4.close();
				System.out.println("flight_assignments row inserted");
			} else {  // no good eid found, need to delay flight
				String query_5 = "INSERT INTO delayed_flights (flno)  " + "VALUES (?)";
				pstmt_5 = connection.prepareStatement(query_5);
				pstmt_5.setInt(1, flight.getFlightNumber());
				pstmt_5.executeUpdate();
				pstmt_5.close();
				System.out.println("***flt " + flight.getFlightNumber() + " from " + flight.getOrigin()
						+ " delayed_flights row inserted");
			}
			connection.commit();
		} finally {
			closeStatement(pstmt_4);
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
