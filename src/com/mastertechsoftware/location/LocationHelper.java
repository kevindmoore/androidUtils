package com.mastertechsoftware.location;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;
import com.mastertechsoftware.util.Logger;

import java.math.BigDecimal;

/**
 * Date: Oct 31, 2010
 */
public class LocationHelper {
	public enum MEASUREMENT {
		METERS,
		YARDS,
		KILOMETERS,
		MILES
	}
	private final static double[] multipliers = {
		1.0,1.0936133,0.001,0.000621371192
	};
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static Location currentBestLocation;
	private static HelperLocationListener networkLocationListener;
	private static HelperLocationListener gpsLocationListener;
	public static Double kEarthRadiusKms = 6376.5;


	public static void startGPSLocationListening(final Context context, long timeout, float distance, final LocationCallback callback) {
		startGPSLocationListening(context, timeout, distance,  callback, null);
	}

	public static void startGPSLocationListening(final Context context, long timeout, float distance, final LocationCallback callback, Looper looper) {
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		removeListener(context, gpsLocationListener);

		// Define a listener that responds to location updates
		gpsLocationListener = new HelperLocationListener(callback);

		// TODO - Seems we have an old item in there.
		removeListener(context, gpsLocationListener);

		// Register the listener with the Location Manager to receive location updates
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
		String criteriaName = locationManager.getBestProvider(criteria, false);
		if (criteriaName != null) {
			if (looper == null) {
				locationManager.requestLocationUpdates(criteriaName, timeout, distance, gpsLocationListener);
			} else {
				locationManager.requestLocationUpdates(criteriaName, timeout, distance, gpsLocationListener, looper);
			}
		} else {
			Logger.error("Could not get a Location Provider");
		}
	}


	public static void startNetworkLocationListening(final Context context, long timeout, float distance, final LocationCallback callback) {
		startNetworkLocationListening(context, timeout, distance, callback, null);
	}

	public static void startNetworkLocationListening(final Context context, long timeout, float distance, final LocationCallback callback, Looper looper) {
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		removeListener(context, networkLocationListener);
		// Define a listener that responds to location updates
		networkLocationListener = new HelperLocationListener(callback);

		// Register the listener with the Location Manager to receive location updates
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
		String criteriaName = locationManager.getBestProvider(criteria, false);
		if (criteriaName != null) {
			if (looper == null) {
				locationManager.requestLocationUpdates(criteriaName, timeout, distance, networkLocationListener);
			} else {
				locationManager.requestLocationUpdates(criteriaName, timeout, distance, networkLocationListener, looper);
			}
		} else {
			Logger.error("Could not get a Location Provider");
		}
	}

	/**
	 * Remove Network Listener
	 * @param context
	 */
	public static void removeNetworkListener(Context context) {
		removeListener(context, networkLocationListener);
		networkLocationListener = null;
	}

	/**
	 * Remove GPS Listener
	 * @param context
	 */
	public static void removeGPSListener(Context context) {
		removeListener(context, gpsLocationListener);
		gpsLocationListener = null;
	}

	public static void removeListener(Context context, HelperLocationListener locationListener) {
		if (locationListener != null) {
			// Acquire a reference to the system Location Manager
			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			locationManager.removeUpdates(locationListener);
		}
	}

	public static void addProximityAlert(Context context, double latitude, double longitude, float radius, PendingIntent intent) {
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.addProximityAlert(latitude, longitude, radius, -1, intent);
	}

	public static void removeProximityAlert(Context context, PendingIntent intent) {
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeProximityAlert(intent);
	}

	public static Location getCurrentLocation(Context context) {
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		LocationProvider provider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);
		if (provider == null) {
			Logger.error("LocationHelper:getCurrentLocation: Provider " + LocationManager.NETWORK_PROVIDER + " does not exist");
			return null;
		}
		return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}

	/**
	 * Determines whether one Location reading is better than the current Location fix
	 *
	 * @param location			The new Location that you want to evaluate
	 * @param currentBestLocation The current Location fix, to which you want to compare the new one
	 * @return true if better
	 */
	protected static boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
													currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	public static double calcDistance(Location startpoint, Location endpoint, MEASUREMENT measurement) {
		return RoundDecimal(calcGeoDistance(startpoint.getLatitude(), startpoint.getLongitude(), endpoint.getLatitude(), endpoint.getLongitude()) * multipliers[measurement.ordinal()], 2);
/*
		double d2r = (180 / Math.PI);
		double distance = 0;

		try {
			double dlong = (endpoint.getLongitude() - startpoint.getLongitude()) * d2r;
			double dlat = (endpoint.getLatitude() - startpoint.getLatitude()) * d2r;
			double a =
				Math.pow(Math.sin(dlat / 2.0), 2)
					+ Math.cos(startpoint.getLatitude() * d2r)
					* Math.cos(endpoint.getLatitude() * d2r)
					* Math.pow(Math.sin(dlong / 2.0), 2);
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
			double d = 6367 * c;

			return d;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
*/
	}

	/**
	 * Method from com.varma.samples.GPSSample
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return
	 */
	private static double calcGeoDistance(final double lat1, final double lon1, final double lat2, final double lon2)
	{
		double distance = 0.0;

		try
		{
			final float[] results = new float[3];

			Location.distanceBetween(lat1, lon1, lat2, lon2, results);

			distance = results[0];
		}
		catch (final Exception ex)
		{
			distance = 0.0;
		}

		return distance;
	}

	public static double RoundDecimal(double value, int decimalPlace)
	{
		BigDecimal bd = new BigDecimal(value);

		bd = bd.setScale(decimalPlace, 6);

		return bd.doubleValue();
	}

	public static double haversine(double Lat1,
								   double Long1, double Lat2, double Long2) {
		/*
			The Haversine formula according to Dr. Math.
			http://mathforum.org/library/drmath/view/51879.html

			dlon = lon2 - lon1
			dlat = lat2 - lat1
			a = (sin(dlat/2))^2 + cos(lat1) * cos(lat2) * (sin(dlon/2))^2
			c = 2 * atan2(sqrt(a), sqrt(1-a))
			d = R * c

			Where
				* dlon is the change in longitude
				* dlat is the change in latitude
				* c is the great circle distance in Radians.
				* R is the radius of a spherical Earth.
				* The locations of the two points in
					spherical coordinates (longitude and
					latitude) are lon1,lat1 and lon2, lat2.
		*/
		double dDistance = Double.MIN_VALUE;
		double dLat1InRad = Lat1 * (Math.PI / 180.0);
		double dLong1InRad = Long1 * (Math.PI / 180.0);
		double dLat2InRad = Lat2 * (Math.PI / 180.0);
		double dLong2InRad = Long2 * (Math.PI / 180.0);

		double dLongitude = dLong2InRad - dLong1InRad;
		double dLatitude = dLat2InRad - dLat1InRad;

		// Intermediate result a.

		double a = Math.pow(Math.sin(dLatitude / 2.0), 2.0) +
			Math.cos(dLat1InRad) * Math.cos(dLat2InRad) *
				Math.pow(Math.sin(dLongitude / 2.0), 2.0);

		// Intermediate result c (great circle distance in Radians).

		double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

		// Distance.

		// const Double kEarthRadiusMiles = 3956.0;

		dDistance = kEarthRadiusKms * c;

		return dDistance;
	}

	/**
	 * Checks whether two providers are the same
	 *
	 * @param provider1
	 * @param provider2
	 * @return if they are the same provider
	 */
	private static boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	static class HelperLocationListener implements LocationListener {
		private LocationCallback callback;

		HelperLocationListener(LocationCallback callback) {
			this.callback = callback;
		}

		public void onLocationChanged(Location location) {
			// Called when a new location is found by the network location provider.
			if (isBetterLocation(location, currentBestLocation)) {
				currentBestLocation = location;
				callback.newLocation(location);
			}
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}
	}
}
