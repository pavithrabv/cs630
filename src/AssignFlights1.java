import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by pavithra on 11/27/16.
 */
public class AssignFlights1 {
    public static void main(String[] args) throws SQLException {
        Connection conn =  JDBCConnection.getConnection();
       AirDB.assignAllFlights(conn);
        AirDB.setNewLocation(conn);
    }
}
