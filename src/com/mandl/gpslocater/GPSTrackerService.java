/*
This file is part of GPSLocater.

    GPSLocater is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GPSLocater is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.mandl.gpslocater;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;

public class GPSTrackerService extends Service implements LocationListener {

	private SmsReceiver mReceiver;

	private final static String TAG_STEUERUNG = "GPSTrackerService";

	private static final Double FIVE_DIGIT = 100000.0D;

	private String strNumber;

	private SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");

	@Override
	public void onCreate() {

		super.onCreate();
		Log.i(TAG_STEUERUNG, "onCreate");
		// REGISTER A BROADCAST RECEIVER
		IntentFilter localIntentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		localIntentFilter.setPriority(2147483646);
		// Instance field for listener

		mReceiver = new SmsReceiver();
		registerReceiver(mReceiver, localIntentFilter);

	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		Log.i(TAG_STEUERUNG, "onDestroy");
		unregisterReceiver(mReceiver);
	}

	boolean SimualtionMode = false;

	boolean isGPSEnabled = false;

	boolean sendLocation = false;

	boolean sendBattLow = false;

	// FLAG FOR NETWORK STATUS
	boolean isNetworkEnabled = false;

	Location location;
	double latitude;
	double longitude;

	// send location via sms
	public static final String ACTION_SMS_AN = "ACTION_SMS_AN";
	public static final String ACTION_SMS_AN_NUMBER = "ACTION_SMS_AN_NUMBER";

	// update loacation internal
	public static final String ACTION_LOCATION = "ACTION_LOCATION";

	public static final String ACTION_BAT_LOW = "ACTION_BAT_LOW";

	// DECLARING A LOCATION MANAGER
	protected LocationManager locationManager;

	private boolean SINGLE_LOCATION_MODE = false;

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		GPSTrackerService getService() {
			return GPSTrackerService.this;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG_STEUERUNG, "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		if (intent != null) {
			doAction(intent);
		}
		// stopped, so return sticky.
		return START_STICKY;
	}

	private void doAction(Intent intent) {
		String action = intent.getAction();
		if (action.equals(ACTION_SMS_AN)) {
			Log.i(TAG_STEUERUNG, ACTION_SMS_AN);
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				strNumber = bundle.getString(ACTION_SMS_AN_NUMBER);
				sendLocation = true;
				getLocation();

			}
		} else if (action.equals(ACTION_LOCATION)) {
			Log.i(TAG_STEUERUNG, ACTION_LOCATION);

			getLocation();

		} else if (action.equals(ACTION_BAT_LOW)) {
			Log.i(TAG_STEUERUNG, ACTION_BAT_LOW);
			if (sendBattLow == false) {
				sendBattLow = true;
				SendBattLow();
			}
		}

	}

	public Location getLocation() {
		try {
			Log.i(TAG_STEUERUNG, "getLocation");

			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_COARSE);

			locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			String provider = locationManager.getBestProvider(criteria, true);

			Log.i(TAG_STEUERUNG, provider);

			// GET GPS STATUS
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// GET NETWORK STATUS
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// NO NETWORK PROVIDERS AVAILABLE
				Log.d(TAG_STEUERUNG, "NO NETWORK PROVIDERS AVAILABLE");
			} else {

				// IF GPS IS ENABLED GET THE LAT/LONG USING GPS SERVICES
				if (isGPSEnabled) {
					if (locationManager != null) {

						if (SINGLE_LOCATION_MODE == true)

						{
							Log.d(TAG_STEUERUNG, "GPS Enabled");
							locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
						} else {
							locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	/**
	 * METHOD TO STOP RECEIVING GPS UPDATES - SAVES BATTERY!!
	 */
	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPSTrackerService.this);
		}
	}

	/**
	 * METHOD TO GET THE LATITUDE OF THE PHONE
	 * 
	 * @return
	 */
	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}

		return latitude;
	}

	/**
	 * METHOD TO GET THE LONGITUDE OF THE PHONE
	 * 
	 * @return
	 */
	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}

		return longitude;
	}

	@Override
	public void onLocationChanged(Location loc) {
		latitude = loc.getLatitude();
		longitude = loc.getLongitude();
		Log.d(TAG_STEUERUNG, "onLocationChanged");
		if (locationManager != null) {
			locationManager.removeUpdates(GPSTrackerService.this);
		}
		if (sendLocation == true) {
			sendLocation = false;
			SendSMS();
		}

	}

	void SendBattLow() {
		SmsManager manager = SmsManager.getDefault();
		String Number;
		String date = df.format(Calendar.getInstance().getTime());
		String text = "GPS Bat low " + date;
		if (SimualtionMode == true) {
			Number = "5556";
		} else {
			Number = strNumber;

		}
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		if (preferences.getBoolean("pref_SendLocationSMS", true)) {
			try {
				manager.sendTextMessage(Number, null, text, null, null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	void SendSMS() {

		SmsManager manager = SmsManager.getDefault();
		String Number;
		String text;
		if (SimualtionMode == true) {
			Number = "5556";
		} else {
			Number = strNumber;

		}
		String date = df.format(Calendar.getInstance().getTime());

		String latitudestr = String.valueOf(Math.round(FIVE_DIGIT * getLatitude()) / FIVE_DIGIT);
		String longitudestr = String.valueOf(Math.round(FIVE_DIGIT * getLongitude()) / FIVE_DIGIT);
		text = "Location: " + date + " " + latitudestr + ",  " + longitudestr;
		Log.d(TAG_STEUERUNG, "SendSMS" + Number + " " + text);
		try {
			manager.sendTextMessage(Number, null, text, null, null);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
