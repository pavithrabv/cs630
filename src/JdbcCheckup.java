/*
// CS636 JdbcCheckup.java, available in $cs636/jdbc,
// i.e. www.cs.umb.edu/cs636/jdbc or /data/htdocs/cs636/jdbc
*/
/*
 * This program can be used to check the JDBC installation.
 * It will insert and select "Hello World" from the database.
 * From Oracle samples, modified to use other databases too
 *
 *//*

// for tunnels needed for off-site access, see document linked from class web page

// for Oracle, run with   
//   java -classpath ojdbc6.jar;. JdbcCheckup
//                                    (use : instead of ; on UNIX)
// Each student gets an account in the one big database
// from cs.umb.edu hosts:
//   host for Oracle: dbs3.cs.umb.edu
//   port: 1521
//   sid: dbs3
// from off-site, with tunnel in place for Oracle on dbs2 using local port 1521:
//   host for Oracle: localhost
//   port: 1521
//   sid: dbs3

// for MySql, run with   
//   java -classpath mysql-connector-java-5.1.39-bin.jar;. JdbcCheckup
//                                    (use : instead of ; on UNIX)
//   Each student gets an individual database: username+db, such as joedb for user joe
// from cs.umb.edu hosts:
//   host for MySql: topcat.cs.umb.edu
//   port: 3306
// from off-site, with tunnel in place for mysql on topcat.cs.umb.edu using local port 3333:
//   host for mysql: localhost
//   port: 3333

// for HSQLDB, first start the HSQLDB server if it's not running:
//   (here . does not need to be on the classpath)
//     java -cp hsqldb.jar org.hsqldb.Server
// then in another window, run JdbcCheckup:
//     java -classpath hsqldb.jar;. JdbcCheckup
//     db or <CR>: <CR>   (nullstring for db name)

import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

class JdbcCheckup {
	public static void main(String args[]) {
		String dbSys = null;
		Scanner in = null;
		try {
			in = new Scanner(System.in);
			System.out
					.println("Please enter information to test connection to the database");
			dbSys = readEntry(in, "Using Oracle (o), MySql (m) or HSQLDB (h)? ");

		} catch (IOException e) {
			System.out.println("Problem with user input, please try again\n");
			System.exit(1);
		}
		// Prompt the user for connect information
		String user = null;
		String password = null;
		String connStr = null;
		String yesNo;
		try {
			if (dbSys.equals("o")) {
				user = readEntry(in, "user: ");
				password = readEntry(in, "password: ");
				yesNo = readEntry(in,
						"use canned Oracle connection string (y/n): ");
				if (yesNo.equals("y")) {
					String host = readEntry(in, "host: ");
					String port = readEntry(in, "port (often 1521): ");
					String sid = readEntry(in, "sid (site id): ");
					connStr = "jdbc:oracle:thin:@" + host + ":" + port + ":"
							+ sid;
				} else {
					connStr = readEntry(in, "connection string: ");
				}
			} else if (dbSys.equals("m")) {// MySQL--
				user = readEntry(in, "user: ");
				password = readEntry(in, "password: ");
				yesNo = readEntry(in,
						"use canned MySql connection string (y/n): ");
				if (yesNo.equals("y")) {
					String host = readEntry(in, "host: ");
					String port = readEntry(in, "port (often 3306): ");
					String db = user + "db";
					connStr = "jdbc:mysql://" + host + ":" + port + "/" + db;
				} else {
					connStr = readEntry(in, "connection string: ");
				}
			} else if (dbSys.equals("h")) { // HSQLDB (Hypersonic) db
				yesNo = readEntry(in,
						"use canned HSQLDB connection string (y/n): ");
				if (yesNo.equals("y")) {
					String db = readEntry(in, "db or <CR>: ");
					connStr = "jdbc:hsqldb:hsql://localhost/" + db;
				} else {
					connStr = readEntry(in, "connection string: ");
				}
				user = "sa";
				password = "";
			} else {
				user = readEntry(in, "user: ");
				password = readEntry(in, "password: ");
				connStr = readEntry(in, "connection string: ");
			}
		} catch (IOException e) {
			System.out.println("Problem with user input, please try again\n");
			System.exit(3);
		}
		System.out.println("using connection string: " + connStr);
		System.out.print("Connecting to the database...");
		System.out.flush();
		JDBCConnection conn = null;
		// Connect to the database
		// Use finally clause to close connection
		try {
			conn = DriverManager.getConnection(connStr, user, password);
			System.out.println("connected.");
			tryWelcomeExperiment(conn);
		} catch (SQLException e) {
			System.out.println("Problem with JDBC JDBCConnection\n");
			printSQLException(e);
			System.exit(4);
		} finally {
			// Close the connection, if it was obtained, no matter what happens
			// above or within called methods
			if (conn != null) {
				try {
					conn.close(); // this also closes the Statement and
									// ResultSet, if any
				} catch (SQLException e) {
					System.out
							.println("Problem with closing JDBC JDBCConnection\n");
					printSQLException(e);
					System.exit(5);
				}
			}
		}
	}
	
	// try to create and use a "welcome" table, or throw if this fails
	static void tryWelcomeExperiment(JDBCConnection conn) throws SQLException {
		// Create a statement
		Statement stmt = conn.createStatement();
		ResultSet rset = null;
		try {
			// We treat this drop table specially to allow it to fail
			// as it will the very first time we run this program
			try {
				stmt.execute("drop table welcome");
			} catch (SQLException e) {
				// assume not there yet, so OK to continue
			}
			// The following database actions are handled normally,
			// i.e., if they fail they will throw a SQLException 
			// and terminate the execution of this method
			// with execution of the finally clause
			stmt.execute("create table welcome(msg char(20))");
			stmt.execute("insert into welcome values ('Hello World!')");
			rset = stmt.executeQuery("select * from welcome");
			while (rset.next())
				System.out.println(rset.getString(1));
			System.out.println("Your JDBC installation is correct.");
		} finally {   // Note: try without catch: let the caller handle
					  // any exceptions of the "normal" db actions. 
			stmt.close(); // clean up statement resources, incl. rset
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
	public static String readEntry(Scanner in, String prompt)
			throws IOException {
		System.out.print(prompt);
		return in.nextLine().trim();
	}
}
*/
