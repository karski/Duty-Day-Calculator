package com.karson.android.dutydaycalculator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

public class CalculatorActivity extends Activity {

	final static String START_HOUR = "startHourS";
	final static String START_MIN = "startMinS";
	final static String END_HOUR = "endHourS";
	final static String END_MIN = "endMinS";
	final static String DIFF_HOUR = "diffHourS";
	final static String DIFF_MIN = "diffMinS";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calculator);

		// set up the action bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("Time Calculator");

		// load saved state
		SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
		((EditText) findViewById(R.id.startHourText)).setText(pref.getString(
				START_HOUR, "0"));
		((EditText) findViewById(R.id.startMinuteText)).setText(pref.getString(
				START_MIN, "0"));
		((EditText) findViewById(R.id.endHourText)).setText(pref.getString(
				END_HOUR, "0"));
		((EditText) findViewById(R.id.endMinuteText)).setText(pref.getString(
				END_MIN, "0"));
		((EditText) findViewById(R.id.dHourText)).setText(pref.getString(
				DIFF_HOUR, "0"));
		((EditText) findViewById(R.id.dMinText)).setText(pref.getString(
				DIFF_MIN, "0"));
	}

	@Override
	public void onStop() {
		SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(START_HOUR,
				((EditText) findViewById(R.id.startHourText)).getText()
						.toString());
		editor.putString(START_MIN,
				((EditText) findViewById(R.id.startMinuteText)).getText()
						.toString());
		editor.putString(END_HOUR, ((EditText) findViewById(R.id.endHourText))
				.getText().toString());
		editor.putString(END_MIN, ((EditText) findViewById(R.id.endMinuteText))
				.getText().toString());
		editor.putString(DIFF_HOUR, ((EditText) findViewById(R.id.dHourText))
				.getText().toString());
		editor.putString(DIFF_MIN, ((EditText) findViewById(R.id.dMinText))
				.getText().toString());

		editor.commit();
		Log.d("SAVE", "saving");
		super.onStop();
	}

	public void swapTime(View v) {
		String tempHour = "0";
		String tempMin = "0";
		EditText startHour = ((EditText) findViewById(R.id.startHourText));
		EditText startMinute = ((EditText) findViewById(R.id.startMinuteText));
		EditText endHour = ((EditText) findViewById(R.id.endHourText));
		EditText endMinute = ((EditText) findViewById(R.id.endMinuteText));
		// do the swap
		tempHour = startHour.getText().toString();
		tempMin = startMinute.getText().toString();
		startHour.setText(endHour.getText().toString());
		startMinute.setText(endMinute.getText().toString());
		endHour.setText(tempHour);
		endMinute.setText(tempMin);
	}

	public void findStartTime(View v) {
		fixTextInput();
		EditText startHour = ((EditText) findViewById(R.id.startHourText));
		EditText startMinute = ((EditText) findViewById(R.id.startMinuteText));
		EditText endHour = ((EditText) findViewById(R.id.endHourText));
		EditText endMinute = ((EditText) findViewById(R.id.endMinuteText));
		EditText dHour = (EditText) findViewById(R.id.dHourText);
		EditText dMin = (EditText) findViewById(R.id.dMinText);

		int hours = 0;
		int minutes = Integer.valueOf(endMinute.getText().toString())
				- Integer.valueOf(dMin.getText().toString());
		while (minutes < 0) {
			hours--;
			minutes += 60;
		}
		hours += (Integer.valueOf(endHour.getText().toString())
				- Integer.valueOf(dHour.getText().toString()));
		while (hours < 0) {
			hours += 24;
		}

		startHour.setText(Integer.toString(hours));
		startMinute.setText(Integer.toString(minutes));
	}

	public void findEndTime(View v) {
		fixTextInput();
		EditText startHour = ((EditText) findViewById(R.id.startHourText));
		EditText startMinute = ((EditText) findViewById(R.id.startMinuteText));
		EditText endHour = ((EditText) findViewById(R.id.endHourText));
		EditText endMinute = ((EditText) findViewById(R.id.endMinuteText));
		EditText dHour = (EditText) findViewById(R.id.dHourText);
		EditText dMin = (EditText) findViewById(R.id.dMinText);

		int hours = 0;
		int minutes = Integer.valueOf(startMinute.getText().toString())
				+ Integer.valueOf(dMin.getText().toString());
		while (minutes > 59) {
			hours++;
			minutes -= 60;
		}
		hours += (Integer.valueOf(startHour.getText().toString()) + Integer
				.valueOf(dHour.getText().toString()));
		if (hours > 24) {
			hours = hours % 24;
		}

		endHour.setText(Integer.toString(hours));
		endMinute.setText(Integer.toString(minutes));
	}

	public void findTimeDifference(View v) {
		fixTextInput();
		EditText startHour = ((EditText) findViewById(R.id.startHourText));
		EditText startMinute = ((EditText) findViewById(R.id.startMinuteText));
		EditText endHour = ((EditText) findViewById(R.id.endHourText));
		EditText endMinute = ((EditText) findViewById(R.id.endMinuteText));
		EditText dHour = (EditText) findViewById(R.id.dHourText);
		EditText dMin = (EditText) findViewById(R.id.dMinText);

		int endH = Integer.valueOf(endHour.getText().toString());

		int minutes = Integer.valueOf(endMinute.getText().toString())
				- Integer.valueOf(startMinute.getText().toString());
		if (minutes < 0) {
			endH--;
			minutes += 60;
		}
		int hours = endH - Integer.valueOf(startHour.getText().toString());
		if (hours < 0) {
			hours += 24;
		}
		dHour.setText(Integer.toString(hours));
		dMin.setText(Integer.toString(minutes));
	}

	// Ensures that values in text inputs are valid time values
	// changes values that do not fit (blank = 0, >60 for mins, >24 for time
	// hours)
	private void fixTextInput() {
		EditText startHour = ((EditText) findViewById(R.id.startHourText));
		EditText startMinute = ((EditText) findViewById(R.id.startMinuteText));
		EditText endHour = ((EditText) findViewById(R.id.endHourText));
		EditText endMinute = ((EditText) findViewById(R.id.endMinuteText));
		EditText dHour = (EditText) findViewById(R.id.dHourText);
		EditText dMin = (EditText) findViewById(R.id.dMinText);

		// fix start times
		if (startHour.getText().length() == 0) {
			startHour.setText("0");
		}
		if (Integer.valueOf(startHour.getText().toString()) > 24) {
			int hour = Integer.valueOf(startHour.getText().toString()) & 24;
			startHour.setText(Integer.toString(hour));
		}
		if (startMinute.getText().length() == 0) {
			startMinute.setText("0");
		} else if (Integer.valueOf(startMinute.getText().toString()) > 59) {
			startMinute.setText("59");
		}
		// fix end times
		if (endHour.getText().length() == 0) {
			endHour.setText("0");
		}
		if (Integer.valueOf(endHour.getText().toString()) > 24) {
			int hour = Integer.valueOf(endHour.getText().toString()) & 24;
			endHour.setText(Integer.toString(hour));
		}
		if (endMinute.getText().length() == 0) {
			endMinute.setText("0");
		} else if (Integer.valueOf(endMinute.getText().toString()) > 59) {
			endMinute.setText("59");
		}
		// fix difference
		if (dHour.getText().length() == 0) {
			dHour.setText("0");
		}
		if (dMin.getText().length() == 0) {
			dMin.setText("0");
		} else if (Integer.valueOf(dMin.getText().toString()) > 59) {
			dMin.setText("59");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, OutputActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		}
		return false;
	}
}
