package com.mandl.gpslocater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartupSteuerung extends BroadcastReceiver {
	private final static String TAG_STEUERUNG = "GPSStartupSteuerung";

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.i(TAG_STEUERUNG, "onReceive");
		Intent startActivity = new Intent(context, GPSTrackActivity.class);
		startActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity.putExtra("immediate", true);
		context.startActivity(startActivity);

	}

}
