package org.opensky.libadsb;

import java.io.Serializable;

import static java.lang.Math.*;

/*
 *  This file is part of org.opensky.libadsb.
 *
 *  org.opensky.libadsb is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  org.opensky.libadsb is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with org.opensky.libadsb.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Container class for WGS84 positions
 *
 * @author Markus Fuchs (fuchs@opensky-network.org)
 * @author Matthias Schäfer (schaefer@opensky-network.org)
 */
public class Position implements Serializable {
	private static final long serialVersionUID = 1562401753853965728L;
	private Double longitude;
	private Double latitude;
	private Double altitude;
	private boolean reasonable;

	public Position() {
		longitude = null;
		latitude = null;
		altitude = null;
		setReasonable(true); // be optimistic :-)
	}

	/**
	 * @param lon longitude in decimal degrees
	 * @param lat latitude in decimal degrees
	 * @param alt altitude in feet
	 */
	public Position(Double lon, Double lat, Double alt) {
		longitude = lon;
		latitude = lat;
		altitude = alt;
		setReasonable(true);
	}

	/**
	 * @return longitude in decimal degrees
	 */
	public Double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude in decimal degrees
	 */
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return latitude in decimal degrees
	 */
	public Double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude in decimal degrees
	 */
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return altitude in feet
	 */
	public Double getAltitude() {
		return altitude;
	}

	/**
	 * @param altitude in feet
	 */
	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}

	/**
	 * Calculates the two-dimensional great circle distance (haversine)
	 * @param other position to which we calculate the distance
	 * @return distance between the this and other position in meters
	 */
	public Double haversine(Position other) {
		double lon0r = toRadians(this.longitude);
		double lat0r = toRadians(this.latitude);
		double lon1r = toRadians(other.longitude);
		double lat1r = toRadians(other.latitude);
		double a = pow(sin((lat1r - lat0r) / 2.0), 2);
		double b = cos(lat0r) * cos(lat1r) * pow(sin((lon1r - lon0r) / 2.0), 2);

		return 6371000.0 * 2 * asin(sqrt(a + b));
	}

	private double[] toECEF () {
		double lon0r = toRadians(this.longitude);
		double lat0r = toRadians(this.latitude);
		double height = tools.feet2Meters(altitude);

		// WGS84 ellipsoid constants
		double a = 6378137.0;
		double b = 6356752.314245;
		double f = (a-b)/a;

		double e2 = f*(2-f);
		double v = a / Math.sqrt(1 - e2*Math.sin(lat0r)*Math.sin(lat0r));

		return new double[] {
				(v + height) * Math.cos(lat0r) * Math.cos(lon0r), // x
				(v + height) * Math.cos(lat0r) * Math.sin(lon0r), // y
				(v * (1 - e2) + height) * Math.sin(lat0r) // z
		};

	}

	/**
	 * Calculate the three-dimensional distance between this and another position.
	 * This method assumes that the coordinates are WGS84.
	 * @param other position
	 * @return 3d distance or null if lat, lon, or alt is missing
	 */
	public Double WGS84Distance3D (Position other) {
		if (latitude == null || longitude == null || altitude == null)
			return null;

		double[] xyz1 = this.toECEF();
		double[] xyz2 = other.toECEF();

		return Math.sqrt(
				Math.pow(xyz2[0] - xyz1[0], 2) +
						Math.pow(xyz2[1] - xyz1[1], 2) +
						Math.pow(xyz2[2] - xyz1[2], 2)
		);
	}

	/**
	 * This is used to mark positions as unreasonable if a
	 * plausibility check fails during decoding. Some transponders
	 * broadcast false positions and if detected, this flag is unset.
	 * Note that we assume positions to be reasonable by default.
	 * @return true if position has been flagged reasonable by the decoder
	 */
	public boolean isReasonable() {
		return reasonable;
	}

	/**
	 * Set/unset reasonable flag.
	 * @param reasonable false if position is considered unreasonable
	 */
	public void setReasonable(boolean reasonable) {
		this.reasonable = reasonable;
	}

	@Override
	public String toString() {
		return "Position{" +
				"longitude=" + longitude +
				", latitude=" + latitude +
				", altitude=" + altitude +
				", reasonable=" + reasonable +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Position)) return false;

		Position position = (Position) o;

		if (reasonable != position.reasonable) return false;
		if (longitude != null ? !longitude.equals(position.longitude) : position.longitude != null) return false;
		if (latitude != null ? !latitude.equals(position.latitude) : position.latitude != null) return false;
		return altitude != null ? altitude.equals(position.altitude) : position.altitude == null;
	}

	@Override
	public int hashCode() {
		int result = longitude != null ? longitude.hashCode() : 0;
		result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
		result = 31 * result + (altitude != null ? altitude.hashCode() : 0);
		result = 31 * result + (reasonable ? 1 : 0);
		return result;
	}
}
