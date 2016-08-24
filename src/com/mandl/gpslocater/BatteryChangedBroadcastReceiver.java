package com.mandl.gpslocater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BatteryChangedBroadcastReceiver extends BroadcastReceiver {
	
	private final static String TAG_STEUERUNG = "GPSBatteryChanged";

	@Override
	public void onReceive(Context context, Intent intent) {

		String intentAction = intent.getAction();

		if (Intent.ACTION_BATTERY_LOW.equalsIgnoreCase(intentAction))
		{	
			Log.i(TAG_STEUERUNG,"onReceive ACTION_BATTERY_LOW");
			Intent a = new Intent(context, GPSTrackerService.class);
			a.setAction(GPSTrackerService.ACTION_BAT_LOW);
			context.startService(a);
			this.abortBroadcast();
		}
	}

}