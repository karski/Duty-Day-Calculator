package com.karson.android.dutydaycalculator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class ProfileEditorActivity extends FragmentActivity implements OnClickListener {
    final int MENUDELETE = 0;
    final int MENUCANCEL = 1;
    final int MENUEXPORT = 2;
    final int MENUSAVE = 3;
    int index = -1; // index of the profile we are editing
    ProfileClass profile;
    boolean save = true; // do you intend to save the file on exit?

    // provide an interface similar to output that allows user to customize
    // individual profiles
    // also add menu items to output profile to user-readable file for sharing
    // and menu item for deleting profile
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_editor);
        if (savedInstanceState != null) {
            Log.d("saved",
                    "resuming? " + savedInstanceState.getBoolean("resuming"));
        }

        index = getIntent().getIntExtra("index", -1);
        if (index < 0 || index >= OutputActivity.profiles.profileList.size()) {
            // generate a new profile
            profile = new ProfileClass("New Profile");
        } else {
            profile = OutputActivity.profiles.profileList.get(index);
        }
        profile.initializeProfile(); //always ensure takeoff and alert are there
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setCustomView(R.layout.profile_name_input);
        getActionBar().setDisplayShowHomeEnabled(false);

        // generate full layout based on profile
        generateTable();

        // assign new item click listener
        findViewById(R.id.editProfileAddRow).setOnClickListener(this);
    }

    private void generateTable() {
        ((TextView) findViewById(R.id.profNameEditText)).setText(profile
                .getProfileName());
        TableLayout table = (TableLayout) findViewById(R.id.editProfileRowHolder);
        table.removeAllViews();
        //save alert and takeoff indexes for repeated use
        int iAlert = profile.getIndexOfAlert(-1);
        int iTakeoff = profile.getIndexOfTakeoff(-1);
        for (int i = 0; i < profile.getLength(); i++) {
            if(i == iAlert || i==iTakeoff){ //use protected row
                TableRow row = (TableRow) LayoutInflater.from(this).inflate(
                        R.layout.editprotectedrowtemplate, null);
                ((TextView) row.findViewById(R.id.editRowName)).setText(profile
                        .getRowName(i));
                ((Spinner) row.findViewById(R.id.editAfterAlert))
                        .setSelection(profile.isRowAfterShow(i) ? 0 : 1);
                ((TextView) row.findViewById(R.id.editRowDeltaTimeHour))
                        .setText("" + profile.getRowHours(i));
                ((TextView) row.findViewById(R.id.editRowDeltaTimeMinute))
                        .setText("" + profile.getRowMinutes(i));
                table.addView(row);
            }else { //use editable row
                TableRow row = (TableRow) LayoutInflater.from(this).inflate(
                        R.layout.editrowtemplate, null);
                ((TextView) row.findViewById(R.id.editRowName)).setText(profile
                        .getRowName(i));
                ((Spinner) row.findViewById(R.id.editAfterAlert))
                        .setSelection(profile.isRowAfterShow(i) ? 0 : 1);
                ((TextView) row.findViewById(R.id.editRowDeltaTimeHour))
                        .setText("" + profile.getRowHours(i));
                ((TextView) row.findViewById(R.id.editRowDeltaTimeMinute))
                        .setText("" + profile.getRowMinutes(i));
                row.findViewById(R.id.editDeleteRow).setOnClickListener(this);
                table.addView(row);
            }
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.editProfileAddRow) {
            TableLayout table = (TableLayout) findViewById(R.id.editProfileRowHolder);
            TableRow row = (TableRow) LayoutInflater.from(this).inflate(
                    R.layout.editrowtemplate, null);
            row.findViewById(R.id.editDeleteRow).setOnClickListener(this);
            table.addView(row);
        } else { // delete appropriate row
            TableLayout table = (TableLayout) findViewById(R.id.editProfileRowHolder);
            table.removeView((View) v.getParent());
            table.invalidate();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // stop overwriting everything on config change!
        Log.d("CONFIG", "changed");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (save) {
            saveProfile();
        }
    }

    private ProfileClass saveProfile() {
        // turn layout into a profile
        ProfileClass edited = new ProfileClass(
                ((TextView) findViewById(R.id.profNameEditText)).getText()
                        .toString());
        TableLayout table = (TableLayout) findViewById(R.id.editProfileRowHolder);
        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            String rowName = ((TextView) row.findViewById(R.id.editRowName))
                    .getText().toString();
            Boolean isAfter = ((Spinner) row.findViewById(R.id.editAfterAlert))
                    .getSelectedItemPosition() == 0 ? true : false;
            int hours = Integer.parseInt(((TextView) row
                    .findViewById(R.id.editRowDeltaTimeHour)).getText()
                    .toString());
            int minutes = Integer.parseInt(((TextView) row
                    .findViewById(R.id.editRowDeltaTimeMinute)).getText()
                    .toString());
            edited.addRow(rowName, isAfter, hours, minutes);
        }
        if (index < 0 || index >= OutputActivity.profiles.profileList.size()) {
            // new profile
            OutputActivity.profiles.profileList.add(edited);
        } else {
            // replace old profile
            OutputActivity.profiles.profileList.remove(index);
            OutputActivity.profiles.profileList.add(index, edited);
        }
        // save all changes to file
        OutputActivity.profiles.exportList(ProfileList.PROFILESTORAGE,
                OutputActivity.profiles.profileList, this);
        return edited;
    }

    // Options menu for this screen
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, MENUSAVE, 1, "Save")
                .setIcon(android.R.drawable.ic_menu_save)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, MENUEXPORT, 0, "Export Profile").setIcon(
                android.R.drawable.ic_menu_save);
        menu.add(0, MENUDELETE, 0, "Delete Profile").setIcon(
                android.R.drawable.ic_menu_delete);
        menu.add(0, MENUCANCEL, 0, "Cancel Changes").setIcon(
                android.R.drawable.ic_menu_close_clear_cancel);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case MENUSAVE:
                // app icon in action bar clicked or save button clicked; go home
                Intent intent = new Intent(this, EditorListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case MENUDELETE:
                AlertDialog.Builder resetbuilder = new AlertDialog.Builder(this);
                resetbuilder.setMessage("Delete Profile?")
                        .setPositiveButton("Yes", defaultsDialogListener)
                        .setNegativeButton("No", defaultsDialogListener)
                        .create().show();
                return true;
            case MENUCANCEL:
                save = false;// finish without saving changes
                finish();
                return true;
            case MENUEXPORT:
                ProfileClass profile = saveProfile();
                // export file named after profile to a default location
                try {
                    File path = new File(Environment.getExternalStorageDirectory(),
                            "Duty Day Calculator");
                    path.mkdir();
                    File filepath = new File(path, profile.getProfileName()
                            + ".txt");
                    FileOutputStream fos = new FileOutputStream(filepath);
                    DataOutputStream out = new DataOutputStream(fos);
                    profile.exportToReadableFile(out);
                    fos.close();
                    Toast.makeText(this,
                            "Profile saved to:" + '\n' + filepath.toString(),
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.d("SAVE", "save err: " + e.toString());
                    Toast.makeText(this, "Error Saving Profile", Toast.LENGTH_SHORT)
                            .show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    DialogInterface.OnClickListener defaultsDialogListener = new DialogInterface.OnClickListener() {
        // @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked, delete and finish
                    if (index >= 0
                            && index < OutputActivity.profiles.profileList.size()) {
                        // delete existing copy and update saved file to reflect
                        // change
                        OutputActivity.profiles.profileList.remove(index);
                        OutputActivity.profiles.exportList(
                                ProfileList.PROFILESTORAGE,
                                OutputActivity.profiles.profileList,
                                getApplicationContext());
                    }
                    save = false;
                    finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // No clicked, simply exit dialog
                    break;
            }
        }
    };
}
