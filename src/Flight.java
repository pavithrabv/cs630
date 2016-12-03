
import java.sql.Timestamp;
/**
 * Created by pavithra on 11/27/16.
 */
public class Flight {
    private long flightNumber ;
    private String origin       ;
    private String destination  ;
    private long distance     ;
    private Timestamp departs      ;
    private Timestamp arrives      ;
    private double price        ;
    private long isProcessed  ;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Flight flight = (Flight) o;

        if (flightNumber != flight.flightNumber) return false;
        if (distance != flight.distance) return false;
        if (Double.compare(flight.price, price) != 0) return false;
        if (isProcessed != flight.isProcessed) return false;
        if (origin != null ? !origin.equals(flight.origin) : flight.origin != null) return false;
        if (destination != null ? !destination.equals(flight.destination) : flight.destination != null) return false;
        if (departs != null ? !departs.equals(flight.departs) : flight.departs != null) return false;
        return arrives != null ? arrives.equals(flight.arrives) : flight.arrives == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (flightNumber ^ (flightNumber >>> 32));
        result = 31 * result + (origin != null ? origin.hashCode() : 0);
        result = 31 * result + (destination != null ? destination.hashCode() : 0);
        result = 31 * result + (int) (distance ^ (distance >>> 32));
        result = 31 * result + (departs != null ? departs.hashCode() : 0);
        result = 31 * result + (arrives != null ? arrives.hashCode() : 0);
        temp = Double.doubleToLongBits(price);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (isProcessed ^ (isProcessed >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Flight{" +
                "flightNumber=" + flightNumber +
                ", origin='" + origin + '\'' +
                ", destination='" + destination + '\'' +
                ", distance=" + distance +
                ", departs=" + departs +
                ", arrives=" + arrives +
                ", price=" + price +
                ", isProcessed=" + isProcessed +
                '}';
    }

    public long getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(long flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    public Timestamp getDeparts() {
        return departs;
    }

    public void setDeparts(Timestamp departs) {
        this.departs = departs;
    }

    public Timestamp getArrives() {
        return arrives;
    }

    public void setArrives(Timestamp arrives) {
        this.arrives = arrives;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(long isProcessed) {
        this.isProcessed = isProcessed;
    }
}
