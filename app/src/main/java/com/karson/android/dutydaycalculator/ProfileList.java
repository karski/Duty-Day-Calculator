package com.karson.android.dutydaycalculator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class ProfileList {
    public static final String PROFILESTORAGE = "ProfileStorageFile";
    public List<ProfileClass> profileList = new ArrayList<ProfileClass>();

    public void exportList(String filename, List<ProfileClass> list, Context context) {
        try {
            OutputStream fos = new BufferedOutputStream(context.openFileOutput(filename,
                    Context.MODE_PRIVATE));
            DataOutputStream out = new DataOutputStream(fos);
            for (ProfileClass profile : list) {
                profile.exportToFile(out);
            }
            out.close();
            fos.close();
        } catch (Exception e) {
            Log.d("DDC", "exportlist err " + e.toString());
        }
    }

    //This should be used to import from internal storage (private storage, not an external file)
    public List<ProfileClass> importList(String filename, Context context) {
        List<ProfileClass> newList = new ArrayList<ProfileClass>();
        try {
            InputStream fis = new BufferedInputStream(context.openFileInput(filename));
            DataInputStream in = new DataInputStream(fis);
            try {
                while (true) {// keep looping until a null profile is returned
                    ProfileClass newProf = new ProfileClass(in);
                    newList.add(newProf);
                }
            } catch (Exception e) {
                Log.d("importList", e.toString());
            }
            in.close();
            fis.close();
            return newList;
        } catch (Exception e) {
            Log.d("DDC", "importlist err " + e.toString());
            return newList;
        }
    }

    public List<ProfileClass> importReadableList(InputStream fis, Context context, Activity thisActivity) {
        List<ProfileClass> newList = new ArrayList<ProfileClass>();
        try {
//            InputStream fis = new BufferedInputStream(new FileInputStream(filename));
            DataInputStream in = new DataInputStream(fis);
            try {
                while (true) {// keep looping until a null profile is returned
                    ProfileClass newProf = new ProfileClass(in, thisActivity);
                    newList.add(newProf);
                }
            } catch (Exception e) {
                Log.d("importList", e.toString());
            }
            in.close();
            fis.close();
            return newList;
        } catch (Exception e) {
            Log.d("DDC", "importlist err " + e.toString());
            return newList;
        }
    }

    // sort profileList by profile name alphabetically
    public void sort() {
        List<ProfileClass> sortedList = new ArrayList<ProfileClass>();
        for (ProfileClass profile : profileList) {
            int index = 0;
            for (; index < sortedList.size(); index++) {
                if (profile.getProfileName().compareToIgnoreCase(
                        profileList.get(index).getProfileName()) < 0) {
                    break;
                }
            }
            sortedList.add(index, profile);
        }
        profileList = sortedList;
    }

    // reset all profiles to the default
    public void resetWithDefault(Context context, Activity thisActivity) {
        profileList = new ArrayList<ProfileClass>();
        // C-17 basic profile Airland
        ProfileClass profile = new ProfileClass("C-17 Basic Airland");
        profile.addRow("No Alcohol", false, 8, 15);
        profile.addRow("Alert", true, 0, 0);
        profile.addRow("Report", true, 1, 0);
        profile.addRow("Takeoff", true, 3, 45);
        profile.addRow("FDP with inop AP", true, 13, 0);
        profile.addRow("Last Training Event", true, 13, 0);
        profile.addRow("AAR", true, 15, 0);
        profile.addRow("Tactical Duty Day", true, 15, 0);
        profile.addRow("FDP", true, 17, 0);
        profile.addRow("CDT", true, 19, 0);
        profileList.add(profile);
        // C-17 augmented profile Airland
        profile = new ProfileClass("C-17 Augmented Airland");
        profile.addRow("No Alcohol", false, 8, 15);
        profile.addRow("Alert", true, 0, 0);
        profile.addRow("Report", true, 1, 0);
        profile.addRow("Takeoff", true, 3, 45);
        profile.addRow("Last Training Event", true, 13, 0);
        profile.addRow("Training FDP with inop AP", true, 13, 0);
        profile.addRow("AAR with 1AC", true, 15, 0);
        profile.addRow("FDP with inop AP", true, 17, 0);
        profile.addRow("Training FDP", true, 17, 0);
        profile.addRow("Tactical Duty Day", true, 19, 0);
        profile.addRow("AAR with 2ACs", true, 19, 0);
        profile.addRow("FDP", true, 25, 0);
        profile.addRow("CDT", true, 25, 45);
        profileList.add(profile);
        // C-17 basic profile Airdrop
        profile = new ProfileClass("C-17 Basic Airdrop");
        profile.addRow("No Alcohol", false, 8, 15);
        profile.addRow("Alert", true, 0, 0);
        profile.addRow("Report", true, 1, 0);
        profile.addRow(" Takeoff", true, 4, 15);
        profile.addRow("FDP with inop AP", true, 13, 0);
        profile.addRow("Last Training Event", true, 13, 0);
        profile.addRow("AAR", true, 15, 0);
        profile.addRow("Tactical Duty Day", true, 15, 0);
        profile.addRow("FDP", true, 17, 0);
        profile.addRow("CDT", true, 19, 0);
        profileList.add(profile);
        // C-17 augmented profile Airdrop
        profile = new ProfileClass("C-17 Augmented Airdrop");
        profile.addRow("No Alcohol", false, 8, 15);
        profile.addRow("Alert", true, 0, 0);
        profile.addRow("Report", true, 1, 0);
        profile.addRow("Takeoff", true, 4, 15);
        profile.addRow("Last Training Event", true, 13, 0);
        profile.addRow("Training FDP with inop AP", true, 13, 0);
        profile.addRow("AAR with 1AC", true, 15, 0);
        profile.addRow("FDP with inop AP", true, 17, 0);
        profile.addRow("Training FDP", true, 17, 0);
        profile.addRow("Tactical Duty Day", true, 19, 0);
        profile.addRow("AAR with 2ACs", true, 19, 0);
        profile.addRow("FDP", true, 25, 0);
        profile.addRow("CDT", true, 25, 45);
        profileList.add(profile);
        // C-130 basic profile
        profile = new ProfileClass("C-130 Basic");
        profile.addRow("No Alcohol", false, 8, 45);
        profile.addRow("Alert", true, 0, 0);
        profile.addRow("Report", true, 1, 0);
        profile.addRow("Takeoff", true, 3, 15);
        profile.addRow("FDP with inop AP", true, 13, 0);
        profile.addRow("Tactical Duty Day", true, 17, 0);
        profile.addRow("FDP", true, 17, 0);
        profile.addRow("CDT", true, 19, 0);
        profileList.add(profile);
        // C-130 augmented profile
        profile = new ProfileClass("C-130 Augmented");
        profile.addRow("No Alcohol", false, 8, 45);
        profile.addRow("Alert", true, 0, 0);
        profile.addRow("Report", true, 1, 0);
        profile.addRow("Takeoff", true, 3, 15);
        profile.addRow("FDP with inop AP", true, 17, 0);
        profile.addRow("Tactical Duty Day", true, 17, 0);
        profile.addRow("FDP", true, 19, 0);
        profile.addRow("CDT", true, 21, 0);
        profileList.add(profile);
        // C-21 profile
        profile = new ProfileClass("C-21");
        profile.addRow("No Alcohol", false, 9, 0);
        profile.addRow("Alert", true, 0, 0);
        profile.addRow("Report", true, 1, 0);
        profile.addRow("Takeoff", true, 3, 0);
        profile.addRow("FDP with inop AP", true, 13, 0);
        profile.addRow("Tactical Duty Day", true, 13, 0);
        profile.addRow("FDP", true, 15, 0);
        profile.addRow("CDT", true, 17, 0);
        profileList.add(profile);
        // C-12 profile
        profile = new ProfileClass("C-12");
        profile.addRow("No Alcohol", false, 9, 0);
        profile.addRow("Alert", true, 0, 0);
        profile.addRow("Report", true, 1, 0);
        profile.addRow("Takeoff", true, 3, 0);
        profile.addRow("FDP with inop AP", true, 13, 0);
        profile.addRow("FDP", true, 17, 0);
        profile.addRow("CDT", true, 17, 45);
        profileList.add(profile);
        // C-5 basic profile
        profile = new ProfileClass("C-5 Basic");
        profile.addRow("No Alcohol", false, 11, 0);
        profile.addRow("Alert", true, 0, 0);
        profile.addRow("Report", true, 1, 0);
        profile.addRow("Takeoff", true, 4, 15);
        profile.addRow("FDP with inop AP", true, 13, 0);
        profile.addRow("AAR", true, 15, 0);
        profile.addRow("Tactical Duty Day", true, 15, 0);
        profile.addRow("FDP", true, 17, 0);
        profile.addRow("CDT", true, 19, 0);
        profileList.add(profile);
        // C-5 basic profile
        profile = new ProfileClass("C-5 Augmented");
        profile.addRow("No Alcohol", false, 11, 0);
        profile.addRow("Alert", true, 0, 0);
        profile.addRow("Report", true, 1, 0);
        profile.addRow("Takeoff", true, 4, 15);
        profile.addRow("FDP with inop AP", true, 17, 0);
        profile.addRow("AAR", true, 15, 0);
        profile.addRow("Tactical Duty Day", true, 19, 0);
        profile.addRow("FDP", true, 25, 0);
        profile.addRow("CDT", true, 25, 45);
        profileList.add(profile);
        // KC-135 basic profile
        profile = new ProfileClass("KC-135 Basic");
        profile.addRow("No Alcohol", false, 7, 45);
        profile.addRow("Alert", true, 0, 0);
        profile.addRow("Report", true, 1, 0);
        profile.addRow("Takeoff", true, 4, 15);
        profile.addRow("FDP with inop AP", true, 13, 0);
        profile.addRow("FDP", true, 17, 0);
        profile.addRow("CDT", true, 19, 0);
        profileList.add(profile);
        // KC-135 augmented profile
        profile = new ProfileClass("KC-135 Augmented");
        profile.addRow("No Alcohol", false, 7, 45);
        profile.addRow("Alert", true, 0, 0);
        profile.addRow("Report", true, 1, 0);
        profile.addRow("Takeoff", true, 4, 15);
        profile.addRow("FDP with inop AP", true, 17, 0);
        profile.addRow("FDP", true, 25, 0);
        profile.addRow("CDT", true, 25, 45);
        profileList.add(profile);
        // KC-10 basic profile
        profile = new ProfileClass("KC-10 Basic");
        profile.addRow("No Alcohol", false, 8, 15);
        profile.addRow("Alert", true, 0, 0);
        profile.addRow("Report", true, 1, 0);
        profile.addRow("Takeoff", true, 4, 15);
        profile.addRow("Tactical Duty Day", true, 15, 0);
        profile.addRow("FDP with inop AP", true, 13, 0);
        profile.addRow("FDP", true, 17, 0);
        profile.addRow("CDT", true, 19, 0);
        profileList.add(profile);
        // KC-10 augmented profile
        profile = new ProfileClass("KC-10 Augmented");
        profile.addRow("No Alcohol", false, 8, 15);
        profile.addRow("Alert", true, 0, 0);
        profile.addRow("Report", true, 1, 0);
        profile.addRow("Takeoff", true, 4, 15);
        profile.addRow("Tactical Duty Day", true, 19, 0);
        profile.addRow("FDP with inop AP", true, 17, 0);
        profile.addRow("FDP", true, 25, 0);
        profile.addRow("CDT", true, 25, 45);
        profileList.add(profile);
        exportList(PROFILESTORAGE, profileList, context);
    }

}
