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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.os.Environment;

public class TopExceptionHandler implements Thread.UncaughtExceptionHandler {

	private Thread.UncaughtExceptionHandler defaultUEH;

	private Activity app = null;

	public TopExceptionHandler(Activity app) {
		this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		this.app = app;
	}

	public void uncaughtException(Thread t, Throwable e) {
		StackTraceElement[] arr = e.getStackTrace();
		String report = e.toString() + "\n\n";
		report += "--------- Stack trace ---------\n\n";
		for (int i = 0; i < arr.length; i++) {
			report += "    " + arr[i].toString() + "\n";
		}
		report += "-------------------------------\n\n";

		// If the exception was thrown in a background thread inside
		// AsyncTask, then the actual exception can be found with getCause
		report += "--------- Cause ---------\n\n";
		Throwable cause = e.getCause();
		if (cause != null) {
			report += cause.toString() + "\n\n";
			arr = cause.getStackTrace();
			for (int i = 0; i < arr.length; i++) {
				report += "    " + arr[i].toString() + "\n";
			}
		}
		report += "-------------------------------\n\n";

		try {
			File log = new File(Environment.getExternalStorageDirectory(),
					"stacktrace.txt");


			BufferedOutputStream trace = new BufferedOutputStream(
					new FileOutputStream(log));

			trace.write(report.getBytes());
			trace.close();
		} catch (IOException ioe) {
			// ...
		}

		defaultUEH.uncaughtException(t, e);
	}
}