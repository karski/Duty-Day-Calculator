package com.karson.android.dutydaycalculator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
//import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

public class OutputActivity extends Activity {
	public static final String PREFS = "DutyDayPrefs";
	private static final int TIMEDIALOG = 0;
	public static final int EMPTYLISTDIALOG = 1;
	Calendar calendar = Calendar.getInstance();
	SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm");
	int timezone = 0;
	public static ProfileList profiles = new ProfileList();

	OnNavigationListener actionNav = new OnNavigationListener(){
		public boolean onNavigationItemSelected(int itemPosition,
				long itemId) {
			update();
			return false;
		}};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.output);
		// assign action to changetime button
		this.findViewById(R.id.outputTimeDisplay).setOnClickListener(
				new OnClickListener() {
					public void onClick(View view) {
						showDialog(TIMEDIALOG);
					}
				});

		//set up action bar drop down navigation
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		// assign action to scrollbar
		SeekBar zoneBar = (SeekBar) findViewById(R.id.timeZoneBar);
		zoneBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekbar, int progress,
					boolean fromuser) {
				// change the timezone to reflect the new level, then update
				timezone = progress - 12;
				update();
			}

			public void onStartTrackingTouch(SeekBar arg0) {
			}

			public void onStopTrackingTouch(SeekBar arg0) {
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		// recreate alert time from saved preferences
		SharedPreferences prefs = this.getSharedPreferences(PREFS, 0);
		calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, prefs.getInt("hour", 0));
		calendar.set(Calendar.MINUTE, prefs.getInt("minute", 0));
		// set scrollbar to correct position
		try {
			SeekBar zoneSeek = (SeekBar) findViewById(R.id.timeZoneBar);
			zoneSeek.setProgress(prefs.getInt("timezone", 0) + 12);
		} catch (Exception e) {
			Log.d("DDC", "onResume " + e.toString());
		}
		// regenerate profile list and generate spinner
		createProfiles();
		// recalculate table
		update();
	}

	@Override
	public void onPause() {
		super.onStop();
		SharedPreferences prefs = this.getSharedPreferences(PREFS, 0);
		SharedPreferences.Editor edit = prefs.edit();
		edit.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
		edit.putInt("minute", calendar.get(Calendar.MINUTE));
		edit.putInt("timezone", timezone);
		//edit.putInt("selectedprof",
		//		((Spinner) findViewById(R.id.profileSelectionSpinner))
		//				.getSelectedItemPosition());
		edit.putInt("selectedprof", getActionBar().getSelectedNavigationIndex());
		Log.d("DDC","saving selected profile position " + getActionBar().getSelectedNavigationIndex());
		edit.commit();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// make sure that profiles are preserved if closed unexpectedly
		profiles.exportList(profiles.PROFILESTORAGE, profiles.profileList, this);
	}

	// Handles all dialogs for this activity
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case TIMEDIALOG:
			return new TimePickerDialog(this, timePickerListener,
					calendar.get(Calendar.HOUR_OF_DAY),
					calendar.get(Calendar.MINUTE), true);
		case EMPTYLISTDIALOG:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			return builder
					.setMessage(
							"The profile list is empty. Would you like to load Defaults?")
					.setPositiveButton("Yes", defaultsDialogListener)
					.setNegativeButton("No", defaultsDialogListener).create();
		default:
			return null;// OOOPS!
		}
	}

	// Listeners for the dialogs defined above
	private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hour, int minute) {
			calendar.setTime(new Date(calendar.getTime().getYear(), calendar
					.getTime().getMonth(), calendar.getTime().getDay(), hour,
					minute));
			update();
		}
	};
	DialogInterface.OnClickListener defaultsDialogListener = new DialogInterface.OnClickListener() {
		// @Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				// Yes button clicked, load defaults
				profiles.resetWithDefault(getApplicationContext());
				createProfiles();
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				// No clicked, simply exit dialog
				break;
			}
		}
	};

	private void update() {
		// update time display and calculated output
		// update displayed time
		TextView alertTime = (TextView) findViewById(R.id.outputTimeDisplay);
		alertTime.setText("Alert Time:  "
				+ timeformat.format(calendar.getTime()) + "Z");
		TextView localTime = (TextView) findViewById(R.id.localTimeText);
		localTime.setText("Local Time: " + (timezone < 0 ? "" : "+") + timezone
				+ ":00");

		// Generate new calculated output
		// clear out all old entries
		TableLayout table = (TableLayout) findViewById(R.id.outputTable);
		table.removeAllViews();

		// generate calculated output
		//Spinner spinner = (Spinner) findViewById(R.id.profileSelectionSpinner);
		try {
			ProfileClass prof = profiles.profileList.get(getActionBar().
					getSelectedNavigationIndex());
			Calendar zoneCalendar = (Calendar) calendar.clone();
			zoneCalendar.add(Calendar.HOUR_OF_DAY, timezone);
			for (int i = 0; i < prof.getLength(); i++) {
				TableRow row = (TableRow) LayoutInflater.from(this).inflate(
						R.layout.rowtemplate, null);
				TextView tTxt = (TextView) row.findViewById(R.id.rowTime);
				tTxt.setText(timeformat.format(prof.getRowTime(zoneCalendar, i)
						.getTime()) + (timezone == 0 ? "Z" : "L"));
				TextView dtTxt = (TextView) row.findViewById(R.id.rowDeltaTime);
				dtTxt.setText(prof.getRowDeltaTimeString(i));
				TextView nameTxt = (TextView) row.findViewById(R.id.rowName);
				nameTxt.setText(prof.getRowName(i));

				table.addView(row);
			}
		} catch (Exception e) {
			Log.d("DDC", "update() " + e.toString());
		}

	}

	// list of arrays where each entry is [name,bool positive,hour,minute]
	// arrays
	public void createProfiles() {
		SharedPreferences prefs = this.getSharedPreferences(PREFS, 0);
		// load saved profile list
		try {
			profiles.profileList = profiles.importList(
					ProfileList.PROFILESTORAGE, this);
		} catch (Exception e) {
			Log.d("Load err", e.toString());
		}
		if (profiles.profileList.size() <= 0) {
			// no profiles in list - ask politely to load defaults
			showDialog(EMPTYLISTDIALOG);
		}
		//make sure selected item is still in the list
		Log.d("ACK","compare "+getActionBar().getNavigationItemCount()+" <= "+prefs.getInt("selectedprof", 0));
		Log.d("ACK","or "+profiles.profileList.size()+" <= "+getActionBar().getSelectedNavigationIndex());
		if(profiles.profileList.size()<=getActionBar().getSelectedNavigationIndex()){
			Log.d("DDC","CreateProfiles found index error");
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("selectedprof", 0);
			edit.apply();
		}
		profiles.sort();

		// generate a list of names:
		List<String> nameList = new ArrayList<String>();
		for (ProfileClass profile : profiles.profileList) {
			nameList.add(profile.getProfileName());
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, nameList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		getActionBar().setListNavigationCallbacks(adapter, actionNav);
		try {
			getActionBar().setSelectedNavigationItem(prefs.getInt("selectedprof", 0));
		}catch (Exception e){
			Log.d("DDC", "spinner assignment error "+e.toString());
		}
	}

	// Options menu for this screen
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, 1, 0, "Time Calculator").setIcon(R.drawable.calc_icon_dark).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(0, 0, 0, "Edit Profiles");//.setIcon(
				//android.R.drawable.ic_menu_edit).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			startActivity(new Intent(OutputActivity.this,
					EditorListActivity.class));
			return true;
		case 1:
			startActivity(new Intent(OutputActivity.this,
					CalculatorActivity.class));
		}
		return super.onOptionsItemSelected(item);
	}

}
