import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by pavithra on 11/27/16.
 */
public class AirDB {
    public static final String ALL_FLIGHTS_QUERY = "select * from flights1 order by departs";
    public static final String ASSIGN_FLIGHT_QUERY = "select aircraft.aid as AID,emp_min_cruise.eid as EID,emp_min_cruise.min_cruise as Min_Cruise from aircraft1 aircraft ,(Select  E.eid, min(A.CRUISINGRANGE) as min_cruise from Aircraft1 A, EMPLOYEES1 E, CERTIFIED1 C, START_LOCATION S where C.AID = A.AID and E.EID = C.EID and S.EID= E.eid and S.CITY = ? and A.CRUISINGRANGE > ? group by E.eid) emp_min_cruise where emp_min_cruise.min_cruise = aircraft.CRUISINGRANGE Order by emp_min_cruise.min_cruise";
    public static final String INSERT_FLIGHTS_QUERY = "insert into FLIGHT_ASSIGNMENTS values (?, ?, ?)";
    public static final String INSERT_DELAYED_FLIGHTS_QUERY = " Insert into DELAYED_FLIGHTS VALUES (?)";
    public static final String INSERT_NEW_LOCATIONS_QUERY = "Insert into new_location (eid, city, arrival_time) ((select FA.EID, F.DESTINATION as City, F.Arrives as Arrival_time from flight_assignments FA, Flights1 F where FA.FLNO = F.FLNO )Union (Select E.eid, S.CITY as City, null as Arrival_time from Employees1 E, START_LOCATION S where  S.EID = E.EID and E.eid not in (select eid from flight_assignments F) ))";
    public static final String ASSIGN_FLIGHTS_IS_PROCESSED_QUERY = "select * from flights1 where is_processed =0 ";
    public static final String UPDATE_FLIGHTS_IS_PROCESSED_QUERY = "UPDATE flights1 SET is_processed = 1 WHERE FLNO = (?)";

    public static final String FLIGHT_FIELD_FLNO = "FLNO";
    public static final String FLIGHT_FIELD_ORIGIN = "ORIGIN";
    public static final String FLIGHT_FIELD_DESTINATION = "DESTINATION";
    public static final String FLIGHT_FIELD_DISTANCE = "DISTANCE";
    public static final String FLIGHT_FIELD_DEPARTS = "DEPARTS";
    public static final String FLIGHT_FIELD_ARRIVES = "ARRIVES";
    public static final String FLIGHT_FIELD_PRICE = "PRICE";
    public static final String FLIGHT_FIELD_IS_PROCESSED = "IS_PROCESSED";

    public static final String AIRCRAFT_FIELD_AID = "AID";
    public static final String EMPLOYEES_FIELD_EID = "EID";
    public static final String AIRCRAFT_FIELD_MIN_CRUISE = "MIN_CRUISE";


    public static void assignAllFlights(Connection conn) throws SQLException {
        Set<Long> assignedEmployees = new HashSet<>();
        
        Statement getAllFlightsStatement = null;
        getAllFlightsStatement = conn.createStatement();
        ResultSet allFlights = getAllFlightsStatement.executeQuery(ALL_FLIGHTS_QUERY);
        while (allFlights.next()) {
            Flight newFlight = createFlightFromResultSet(allFlights);

            if (!assignFlight(newFlight, conn, assignedEmployees)) {
                insertIntoDelayedFlights(conn, newFlight.getFlightNumber());
                //System.out.println("flights not assigned :" + newFlight.getFlightNumber());
            }

        }
    }

    private static Flight createFlightFromResultSet(ResultSet allFlights) throws SQLException {
        Flight newFlight = new Flight();
        newFlight.setFlightNumber(allFlights.getLong(FLIGHT_FIELD_FLNO));
        newFlight.setOrigin(allFlights.getString(FLIGHT_FIELD_ORIGIN));
        newFlight.setDestination(allFlights.getString(FLIGHT_FIELD_DESTINATION));
        newFlight.setDistance(allFlights.getLong(FLIGHT_FIELD_DISTANCE));
        newFlight.setDeparts(allFlights.getTimestamp(FLIGHT_FIELD_DEPARTS));
        newFlight.setArrives(allFlights.getTimestamp(FLIGHT_FIELD_ARRIVES));
        newFlight.setPrice(allFlights.getDouble(FLIGHT_FIELD_PRICE));
        newFlight.setIsProcessed(allFlights.getLong(FLIGHT_FIELD_IS_PROCESSED));
        return newFlight;
    }

    private static boolean assignFlight(Flight flight, Connection conn, Set<Long> assignedEmployees) throws SQLException {
        boolean flightAssigned = false;
        PreparedStatement assignFlightStatement = null;
        assignFlightStatement = conn.prepareStatement(ASSIGN_FLIGHT_QUERY);
        assignFlightStatement.setString(1, flight.getOrigin());
        assignFlightStatement.setLong(2, flight.getDistance());
        ResultSet filteredFlights = assignFlightStatement.executeQuery();
        while (filteredFlights.next()) {
            Long eid = filteredFlights.getLong(EMPLOYEES_FIELD_EID);
            Long aid = filteredFlights.getLong(AIRCRAFT_FIELD_AID);
            if (!assignedEmployees.contains(eid)) {
                //System.out.println("Inserting flight:"+ flight.getFlightNumber()+", eid: "+eid+", aid:"+aid);
                try {
                    insertIntoFlightAssignments(conn, flight.getFlightNumber(), aid, eid);
                    assignedEmployees.add(eid);
                    flightAssigned = true;
                    return flightAssigned;
                } catch (SQLException e) {
                    System.out.println("Inserting flight:" + flight.getFlightNumber() + ", eid: " + eid + ", aid:" + aid);
                    e.printStackTrace();
                }
            }
            /*System.out.println(filteredFlights.getLong(AIRCRAFT_FIELD_AID));
            System.out.println(filteredFlights.getLong(EMPLOYEES_FIELD_EID));
            System.out.println(filteredFlights.getLong(AIRCRAFT_FIELD_MIN_CRUISE));*/
        }
        return flightAssigned;
    }


    private static int insertIntoFlightAssignments(Connection conn, long flightNumber, long aid, long eid) throws SQLException {
        PreparedStatement insertFlightStatement = conn.prepareStatement(INSERT_FLIGHTS_QUERY);
        insertFlightStatement.setLong(1, flightNumber);
        insertFlightStatement.setLong(2, aid);
        insertFlightStatement.setLong(3, eid);
        return insertFlightStatement.executeUpdate();

    }

    private static int insertIntoDelayedFlights(Connection conn, long flightNumber) throws SQLException {
        PreparedStatement insertDelayedFlightsStatement = conn.prepareStatement(INSERT_DELAYED_FLIGHTS_QUERY);
        insertDelayedFlightsStatement.setLong(1, flightNumber);
        return insertDelayedFlightsStatement.executeUpdate();
    }


    public static void test(Connection conn) throws SQLException {
        Statement getAllFlightsStatement = conn.createStatement();
        getAllFlightsStatement.execute("create table welcome(msg char(20))");
    }

    public static int setNewLocation(Connection conn) throws SQLException {
        PreparedStatement insertIntoNewLocationStatement = conn.prepareStatement(INSERT_NEW_LOCATIONS_QUERY);
        return insertIntoNewLocationStatement.executeUpdate();
    }


    public static void assignFlight() throws SQLException {
        Connection conn = JDBCConnection.getConnection();
        Set<Long> assignedEmployees = new HashSet<>();
        while (true) {
            PreparedStatement assignFlightIsProcessedStatement = conn.prepareStatement(ASSIGN_FLIGHTS_IS_PROCESSED_QUERY);
            assignFlightIsProcessedStatement.setMaxRows(1);
            ResultSet flightToProcess = assignFlightIsProcessedStatement.executeQuery();
            if(flightToProcess.next()){
                Flight newFlight = createFlightFromResultSet(flightToProcess);
                if (!assignFlight(newFlight, conn, assignedEmployees)) {
                    insertIntoDelayedFlights(conn, newFlight.getFlightNumber());
                }
                PreparedStatement assignIsProcessedStatement = conn.prepareStatement(UPDATE_FLIGHTS_IS_PROCESSED_QUERY);
                assignIsProcessedStatement.setLong(1, newFlight.getFlightNumber());
                assignIsProcessedStatement.executeUpdate();
                System.out.println("updated flight :" + newFlight.getFlightNumber());
            }else{
                return;
            }
        }
    }
}
