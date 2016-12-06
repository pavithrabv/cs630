/**
 * AssignFlights1, a JDBC program for Homework 4 of cs430/630 Fall 2016
 * Reads tables flights1, aircraft1, certified1, and start_location to
 * find employees (pilots) certified for aircraft that have cruising range
 * sufficient to fly as directed in flights1 to the destination in one hop.
 * The pilots need to be at the origin city, as recorded in start_location. 
 * The program fills tables flight_assignmants and delayed_flights with
 * successful assignments and flights delayed because no pilot is available,
 * respectively. Table new_location is filled to record where the pilots end up.
 * 
 * This version of the program uses a cursor across the flights1 table at
 * top level, and fills a Flight object with information on a single flight.
 * Then sssignFlight(Flight flight) is called to do the work for one flight.
 * After the flights are processed this way, the new_location table is filled.
 * 
 * The program produces more output than a production program would, to aid
 * in its understanding. Student solutions may produce much less output.
 * The official results of this program are in tables assigned_flights,
 * delayed_flights, and new_location.
 */
import java.io.IOException;
import java.util.Scanner;
import java.sql.*;
public class AssignFlights1 {
	private static Connection connection = null;

	public static void main(String args[]) {
		String dbSys = null;
		Scanner in = null;
		try {
			in = new Scanner(System.in);
			System.out.println("Please enter information for connection to the database");
			dbSys = readEntry(in, "Using Oracle (o) or MySql (m)? ");
		} catch (IOException e) {
			System.out.println("Problem with user input, please try again\n");
			System.exit(1);
		}

		// Prompt the user for connect information
		String username = null;
		String password = null;
		String connStr = null;
		try {
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
			System.exit(2);
		}
		System.out.println("using connection string: " + connStr);
		System.out.print("Connecting to the database...");

		try {
			connection = getConnected(connStr, username, password);
		} catch (SQLException except) {
			System.out.println("Problem with JDBC Connection");
			System.out.println(except.getMessage());
			System.exit(3);
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
			System.exit(4);
		}

		// reads the flights1 table in order of departure time
		PreparedStatement pstmt_1 = null;
		String query_1 = "SELECT *  FROM flights1  ORDER BY departs";
		try {
			pstmt_1 = connection.prepareStatement(query_1);
			pstmt_1.clearParameters();
			ResultSet rs_1 = pstmt_1.executeQuery();

			// process by each row of flights table
			while (rs_1.next()) {
				int flno = rs_1.getInt("flno"); // Retrieve the column value
				String origin = rs_1.getString("origin");
				String destination = rs_1.getString("destination");
				int distance = rs_1.getInt("distance");
				Timestamp departs = rs_1.getTimestamp("departs");
				Timestamp arrives = rs_1.getTimestamp("arrives");
				double price = rs_1.getFloat("price");
				int isProcessed = rs_1.getInt("is_processed");

				Flight flight = new Flight(flno, origin, destination, distance, departs, arrives, price, isProcessed); // store
																														// the
				// find an airplane and a certified pilot for each flight
				assignFlight(flight);
			}
			findFinalLocations();
		} catch (SQLException except) {
			System.out.println("Problem handled in main loop");
			printSQLException(except);
		} finally {
			// Close the PreparedStatement & its ResultSet (if they exist)
			closeStatement(pstmt_1);
		}
	}

	private static void assignFlight(Flight flight) throws SQLException {

		String query_2 = "SELECT A.aid, A.aname, E.eid, A.cruisingrange   "
				+ " FROM aircraft1 A, certified1 E, start_location sl  "
				+ " WHERE (E.aid = A.aid and sl.eid = e.eid) AND A.cruisingrange > ? "
				+ " and E.eid not in (select fa.eid from flight_assignments fa) " + " and sl.city = ?"
				+ "ORDER BY A.cruisingrange";

		PreparedStatement pstmt_2 = null, pstmt_3 = null, pstmt_4 = null;
		int aid = -1, eid = -1;
		try {
			pstmt_2 = connection.prepareStatement(query_2);
			pstmt_2.clearParameters();
			pstmt_2.setInt(1, flight.getDistance()); // feed the value to "?"
			pstmt_2.setString(2, flight.getOrigin());
			String origin = flight.getOrigin();
			ResultSet rs_2 = pstmt_2.executeQuery();
			if (rs_2.next()) {
				aid = rs_2.getInt("aid");
				eid = rs_2.getInt("eid");
				int cruisingrange = rs_2.getInt("cruisingrange");
				System.out.println("for flt " + flight.getFlightNumber() + ", from " + origin + ", found: " + aid + " "
						+ eid + " (" + cruisingrange + ")");
				while (rs_2.next()) {
					int aid2 = rs_2.getInt("aid");
					int eid2 = rs_2.getInt("eid");
					int cruisingrange2 = +rs_2.getInt("cruisingrange");
					if (cruisingrange2 > cruisingrange)
						break; // not a tie
					System.out.println("  found tie:" + aid2 + " " + eid2 + " (" + cruisingrange2 + ")");
				}
			}
		} finally {
			closeStatement(pstmt_2);
		}
		try {
			// Inserts a new row in flight_assignments or delayed_flights
			if ((aid > 0) || (eid > 0)) 
			{
				String query_3 = "INSERT INTO flight_assignments (flno, aid, eid)  " + "VALUES (?, ?, ?)";
				pstmt_3 = connection.prepareStatement(query_3);
				pstmt_3.setInt(1, flight.getFlightNumber());
				pstmt_3.setInt(2, aid);
				pstmt_3.setInt(3, eid);
				pstmt_3.executeUpdate();
				System.out.println("flight_assignments row inserted");
			}

			else {
				String query_4 = "INSERT INTO delayed_flights (flno)  " + "VALUES (?)";
				pstmt_4 = connection.prepareStatement(query_4);
				pstmt_4.setInt(1, flight.getFlightNumber());
				pstmt_4.executeUpdate();
				System.out.println("***flt " + flight.getFlightNumber() + " from " + flight.getOrigin()
						+ " delayed_flights row inserted");
			}

		} finally {
			// Close the PreparedStatements & their ResultSets (if they exist)
			closeStatement(pstmt_3);
			closeStatement(pstmt_4);
		}
	}
	
	private static void findFinalLocations() throws SQLException {
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
