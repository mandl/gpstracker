
package com.mandl.gpslocater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

	private final static String TAG_STEUERUNG = "GPSSmsReceiver";

	private static final String SMS_EXTRA_NAME = "pdus";
	
	private static final String SECRET_LOCATION_B = "*3*";

	private static final String DEFAULT_CODE = "1234";

	private SharedPreferences preferences;

	private Context context;

	private String secretCode = null;

	public void onReceive(final Context ctx, Intent intent) {
		// GET SMS MAP FROM INTENT
		Bundle extras = intent.getExtras();
		context = ctx;

		Log.i(TAG_STEUERUNG, "onReceive");
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		secretCode = preferences.getString("pref_Code", DEFAULT_CODE);
		boolean senderEnabled = preferences.getBoolean("pref_SendLocationSMS",
				true);

		if (extras != null) {

			// GET THE RECEIVED SMS ARRAY
			Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);

			for (int i = 0; i < smsExtra.length; ++i) {

				// GET THE MESSAGE
				SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);

				// PARSE THE MESSAGE BODY
				String body = sms.getMessageBody().toString();
				String address = sms.getOriginatingAddress();

				Log.d(TAG_STEUERUNG, body + "  " + address);

				if (body.equals(SECRET_LOCATION_B + secretCode)) {
					Log.d(TAG_STEUERUNG, "SECRET_LOCATION_B + secretCode");

					if (senderEnabled == true) {
						Intent a = new Intent(context, GPSTrackerService.class);
						a.setAction(GPSTrackerService.ACTION_SMS_AN);
						a.putExtra(GPSTrackerService.ACTION_SMS_AN_NUMBER,
								address);
						context.startService(a);
						this.abortBroadcast();
					}
				}
			}
		}
	}
}
