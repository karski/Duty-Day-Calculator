package com.karson.android.dutydaycalculator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.TimeZone;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

//import android.widget.Spinner;

public class OutputActivity extends FragmentActivity{
    public static final String PREFS = "DutyDayPrefs";
    private static final int TIMEDIALOG = 0;
    public static final int EMPTYLISTDIALOG = 1;
    public static Calendar calendar = Calendar.getInstance();
    SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm");
    TimezoneEntry displayedTimezone = null;
    int timezoneIndex = 0;
    int zuluIndex = 0;
    boolean alertMode = true; //determines what anchors time calculations - alert (true) or takeoff (false)
    boolean autoTZ = false;
    ArrayList<TimezoneEntry> timezoneList = new ArrayList<TimezoneEntry>();
    public static ProfileList profiles = new ProfileList();
    Spinner navSpinner;

    public static class TimeDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);
            //return new TimePickerDialog(getActivity(), this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),true);
            return new TimePickerDialog(getActivity(), this, getArguments().getInt("hour"), getArguments().getInt("minute"), true);
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Log.d("Duty", "onTimeSet: " + hourOfDay + ":" + minute);
            calendar.getInstance();
            calendar.set(calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(calendar.MINUTE, minute);
            ((OutputActivity) getContext()).update();  //OMFG this took all day to find!!!!!!!
        }

    }



    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.output);
        // assign action to changetime button
        this.findViewById(R.id.outputTimeDisplay).setOnClickListener(
                new OnClickListener() {
                    public void onClick(View view) {
                        //showDialog(TIMEDIALOG);
                        Bundle params = new Bundle();
                        params.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
                        params.putInt("minute", calendar.get(Calendar.MINUTE));
                        TimeDialog timeFrag = new TimeDialog();  //new TimeDialog();
                        timeFrag.setArguments(params);
                        timeFrag.show(getSupportFragmentManager(), "TimePicker");
                    }
                });

        //set up action bar drop down navigation
        navSpinner = new Spinner(this,Spinner.MODE_DROPDOWN);
        navSpinner.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        navSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                update();
            }
            public void onNothingSelected(AdapterView<?> parent) {
                update();
            }
        });

        getActionBar().setCustomView(navSpinner);

        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        //getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);


        ArrayList<TimezoneEntry> tzList = new ArrayList<TimezoneEntry>();
        Date today = new Date();
        for (int offset = -15 * 60 * 60000; offset <= (14 * 60 * 60000); offset += (15 * 60000)) {
            Set<String> tzIDs = TimeZone.getAvailableIDs(TimeZone.SystemTimeZoneType.CANONICAL_LOCATION, "US", offset);
            if (tzIDs.isEmpty()) { //allow all regions if Americas doesn't populate
                tzIDs = TimeZone.getAvailableIDs(TimeZone.SystemTimeZoneType.CANONICAL_LOCATION, "GB", offset);
            }
            if (tzIDs.isEmpty()) { //allow all regions if Americas doesn't populate
                tzIDs = TimeZone.getAvailableIDs(TimeZone.SystemTimeZoneType.CANONICAL_LOCATION, "DE", offset);
            }
            if (tzIDs.isEmpty()) { //allow all regions if Americas doesn't populate
                tzIDs = TimeZone.getAvailableIDs(TimeZone.SystemTimeZoneType.CANONICAL_LOCATION, null, offset);
            }
            if (!tzIDs.isEmpty()) {
                //record all the offsets with daylight corrections
                for (String availableID : tzIDs) {
                    //Log.d("TZ", TimeZone.getTimeZone(availableID).getDisplayName(TimeZone.getTimeZone(availableID).inDaylightTime(new Date()), TimeZone.SHORT_GMT) + " : " + (offset==0?"ZULU": TimeZone.getTimeZone(availableID).getDisplayName()));
                    TimeZone tz = TimeZone.getTimeZone(availableID);
                    tzList.add(new TimezoneEntry(tz, today));
                }
            }
        }
        //put in order of offsets
        Collections.sort(tzList, new Comparator<TimezoneEntry>() {
            public int compare(TimezoneEntry o1, TimezoneEntry o2) {
                return o1.compareTo(o2);
            }
        });
        //select one zone id from each offset to act as an entry
        timezoneList = new ArrayList<TimezoneEntry>();
        for (TimezoneEntry entry : tzList) {
            if (timezoneList.isEmpty() || (entry.currentOffset > timezoneList.get(timezoneList.size() - 1).currentOffset)) {
                timezoneList.add(entry);
                if (entry.currentOffset == 0) {
                    zuluIndex = timezoneList.indexOf(entry);
                    timezoneIndex = zuluIndex;
                }
            }
        }


        // assign action to scrollbar
        SeekBar zoneBar = (SeekBar) findViewById(R.id.timeZoneBar);
        zoneBar.setMax(timezoneList.size() - 1);
        zoneBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekbar, int progress,
                                          boolean fromuser) {
                // change the timezone to reflect the new level, then update
                timezoneIndex = progress;//- 12;
                update();
            }

            public void onStartTrackingTouch(SeekBar arg0) {
            }

            public void onStopTrackingTouch(SeekBar arg0) {
            }
        });

        // assign action to mode toggle button
        final Button modeToggle = (Button) findViewById(R.id.alertModeToggleButton);
        modeToggle.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                alertMode = !alertMode;
                update();
            }
        });

        // assign action to auto timezone switch
        final Switch autoTimezoneSwitch = (Switch) findViewById(R.id.timeZoneAutoSwitch);
        autoTimezoneSwitch.setChecked(autoTZ);
        autoTimezoneSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoTZ = isChecked;
                update();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        // recreate alert time from saved preferences
        SharedPreferences prefs = this.getSharedPreferences(PREFS, 0);

        alertMode = prefs.getBoolean("alertMode", true);
        autoTZ = prefs.getBoolean("autoTimezoneMode", false);
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, prefs.getInt("hour", 0));
        calendar.set(Calendar.MINUTE, prefs.getInt("minute", 0));
        // set scrollbar to correct position - might be out of bounds, so tread carefully
        try {
            SeekBar zoneSeek = (SeekBar) findViewById(R.id.timeZoneBar);
            timezoneIndex = prefs.getInt("timezone", zuluIndex);
            zoneSeek.setProgress(timezoneIndex);
        } catch (Exception e) {
            Log.d("DDC", "onResume " + e.toString());
            SeekBar zoneSeek = (SeekBar) findViewById(R.id.timeZoneBar);
            timezoneIndex = zuluIndex;
            zoneSeek.setProgress(timezoneIndex);
        }
        // regenerate profile list and generate spinner
        createProfiles();
        // recalculate table
        update();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences prefs = this.getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
        edit.putInt("minute", calendar.get(Calendar.MINUTE));
        edit.putInt("timezone", timezoneIndex);
        edit.putBoolean("alertMode", alertMode);
        edit.putBoolean("autoTimezoneMode", autoTZ);
        edit.putInt("selectedprof", navSpinner.getSelectedItemPosition());
        Log.d("DDC", "saving selected profile position " + navSpinner.getSelectedItemPosition());
        edit.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // make sure that profiles are preserved if closed unexpectedly
        profiles.exportList(profiles.PROFILESTORAGE, profiles.profileList, this,this);
    }


    private void update() {
        // update time display and calculated output
        // update displayed time
        Button modeToggle = (Button) findViewById(R.id.alertModeToggleButton);
        modeToggle.setText((alertMode ? "Alert" : "Takeoff") + " Time:");
        TextView alertTime = (TextView) findViewById(R.id.outputTimeDisplay);
        alertTime.setText(timeformat.format(calendar.getTime()) + "Z");
        SeekBar zoneBar = (SeekBar) findViewById(R.id.timeZoneBar);
        zoneBar.setEnabled(!autoTZ);
        if (autoTZ) {
            displayedTimezone = new TimezoneEntry(TimeZone.getDefault(), new Date());
        } else {
            displayedTimezone = timezoneList.get(timezoneIndex);
        }
        TextView localTime = (TextView) findViewById(R.id.localTimeText);
        localTime.setText("Local Time: " + (displayedTimezone.offsetHours >= 0 ? "+" : "") + displayedTimezone.offsetHours + ":" +
                (displayedTimezone.offsetMinutes < 10 ? "0" : "") + displayedTimezone.offsetMinutes +
                " (" + displayedTimezone.name + ")");
        //localTime.setText("Local Time: " + (timezone < 0 ? "" : "+") + timezone				+ ":00");


        // Generate new calculated output
        // clear out all old entries
        TableLayout table = (TableLayout) findViewById(R.id.outputTable);
        table.removeAllViews();

        // generate calculated output
        try {
            ProfileClass prof = profiles.profileList.get(navSpinner.getSelectedItemPosition());
            Calendar zoneCalendar = (Calendar) calendar.clone();
            zoneCalendar.add(Calendar.HOUR_OF_DAY, displayedTimezone.offsetHours);
            zoneCalendar.add(Calendar.MINUTE, (displayedTimezone.currentOffset >= 0 ? 1 : -1) * displayedTimezone.offsetMinutes);

            for (int i = 0; i < prof.getLength(); i++) {
                TableRow row = (TableRow) LayoutInflater.from(this).inflate(
                        R.layout.rowtemplate, null);
                TextView tTxt = (TextView) row.findViewById(R.id.rowTime);
                tTxt.setText(timeformat.format(prof.getRowTime(zoneCalendar, i, alertMode)
                        .getTime()) + (displayedTimezone.currentOffset == 0 ? "Z" : "L"));
                TextView dtTxt = (TextView) row.findViewById(R.id.rowDeltaTime);
                dtTxt.setText(prof.getRowDeltaTimeString(i, alertMode));
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
                    ProfileList.PROFILESTORAGE, this,this);
        } catch (Exception e) {
            Log.d("Load err", e.toString());
        }
        if (profiles.profileList.size() <= 0) {
            // no profiles in list - ask politely to load defaults
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("The profile list is empty. Would you like to load Defaults?");
            // Add the buttons
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    profiles.resetWithDefault(getApplicationContext(),OutputActivity.this);
                    createProfiles();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            //Display
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        //make sure selected item is still in the list
        Log.d("ACK", "compare " + navSpinner.getCount() + " <= " + prefs.getInt("selectedprof", 0));
        Log.d("ACK", "or " + profiles.profileList.size() + " <= " + navSpinner.getSelectedItemPosition());
        if (profiles.profileList.size() <= navSpinner.getSelectedItemPosition()) {
            Log.d("DDC", "CreateProfiles found index error");
            SharedPreferences.Editor edit = prefs.edit();
            edit.putInt("selectedprof", 0);
            edit.commit();
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
        navSpinner.setAdapter(adapter);
        try {
            navSpinner.setSelection(prefs.getInt("selectedprof", 0));
        } catch (Exception e) {
            Log.d("DDC", "spinner assignment error " + e.toString());
        }
    }

    // Options menu for this screen
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, 1, 0, "Time Calculator").setIcon(R.drawable.calc_icon_dark).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, 0, 0, "Edit Profiles").setIcon(android.R.drawable.ic_menu_edit).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);//.setIcon(
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
