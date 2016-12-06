import java.sql.*;
import java.io.Serializable;

public class Flight implements Serializable {
	private static final long serialVersionUID = 1L;
	private int flino;
	private String origin;
	private String destination;
	private int distance;
	private Timestamp departs;
	private Timestamp arrives;
	private double price;
	private int is_processed;

	public Flight(int flino, String origin, String destination, int distance, Timestamp departs, Timestamp arrives,
			double price, int is_processed) {
		this.flino = flino;
		this.origin = origin;
		this.destination = destination;
		this.distance = distance;
		this.departs = departs;
		this.arrives = arrives;
		this.price = price;
		this.is_processed = is_processed;

	}

	public int getFlightNumber() {
		return this.flino;
	}

	public void setFlightNumber(int flino_new) {
		this.flino = flino_new;
	}

	public String getOrigin() {
		return this.origin;
	}

	public void setOrigin(String origin_new) {
		this.origin = origin_new;
	}

	public String getDestination() {
		return this.destination;
	}

	public void setDestination(String destination_new) {
		this.destination = destination_new;
	}

	public int getDistance() {
		return this.distance;
	}

	public void setDistance(int distance_new) {
		this.flino = distance_new;
	}

	public Timestamp getDeparts() {
		return this.departs;
	}

	public void setDeparts(Timestamp departs_new) {
		this.departs = departs_new;
	}

	public Timestamp getArrives() {
		return this.arrives;
	}

	public void setArrives(Timestamp arrives_new) {
		this.arrives = arrives_new;
	}

	public double getPrice() {
		return this.price;
	}

	public void setPrice(double price_new) {
		this.price = price_new;
	}

	public int getIs_processed() {
		return this.is_processed;
	}

	public void setIs_processed(int is_processed_new) {
		this.is_processed = is_processed_new;
	}

}