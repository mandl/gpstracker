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