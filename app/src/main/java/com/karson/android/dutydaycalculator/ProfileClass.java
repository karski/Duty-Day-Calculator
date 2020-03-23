package com.karson.android.dutydaycalculator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.util.Log;

public class ProfileClass {

    public static int AFTERINDEX = 0; // index of value that tells if time is
    // after alert
    public static int HOURINDEX = 1; // index of value holding hour
    public static int MINUTEINDEX = 2; // index of value holding minutes
    private String name = "";
    private List<String> rowNames = new ArrayList<String>(); // list holding
    // titles of
    // each row
    private List<int[]> rowValues = new ArrayList<int[]>(); // list of row value
    // arrays

    // each array will hold values for the time off of show time
    // {+-1:before or after 0 (usually alert) time; hours; minutes}

    public ProfileClass(String profName) {
        name = profName;
    }

    //add the default entries into a profile
    public void initializeProfile(){
        if(getIndexOfAlert(-1)<0){
            addRow("Alert",true,0,0);
        }
        if(getIndexOfTakeoff(-1)<0){
            addRow("Takeoff",true,0,0);
        }
    }

    public void addRow(String name, boolean isAfterShow, int hours, int minutes) {
        // make sure values are within range and not reserved
        if (hours >= 0 && minutes >= 0 && minutes < 60){// && name != "Alert" && name != "Takeoff") {
            int afterShow = 1;
            if (!isAfterShow) {
                afterShow = -1;
            }
            // find appropriate index for immediate sorting
            int index = 0;
            int sortScore = afterShow * (hours * 100 + minutes);
            for (; index < rowValues.size(); index++) {
                int thisScore = rowValues.get(index)[AFTERINDEX]
                        * (rowValues.get(index)[HOURINDEX] * 100 + rowValues
                        .get(index)[MINUTEINDEX]);
                if (sortScore < thisScore) {
                    break;
                }
            }
            // create new rows
            rowNames.add(index, name);
            rowValues.add(index, new int[]{afterShow, hours, minutes});
        }
    }


    // Return name of profile
    public String getProfileName() {
        return name;
    }

    // Return number of rows in profile
    public int getLength() {
        return rowNames.size();
    }

    // Return index of the row that has the entry "Alert"
    public int getIndexOfAlert(int defaultIndex) {
        int result= rowNames.indexOf("Alert");
        if(result<0){
            for(int i=0;i<rowNames.size();i++){
                if(rowNames.get(i).toLowerCase().contains("alert")){
                    return i;
                }
            }
            return defaultIndex; //full search did not turn up a correct entry, use the first one
        }else{
            return result; //indexOf had valid result
        }
    }

    // Return index of the row that has the entry "Takeoff"
    public int getIndexOfTakeoff(int defaultIndex) {
        int result= rowNames.indexOf("Takeoff");
        if(result<0){
            for(int i=0;i<rowNames.size();i++){
                if(rowNames.get(i).toLowerCase().contains("takeoff")){
                    return i;
                }
            }
            return defaultIndex; //full search did not turn up a correct entry, use the first one
        }else{
            return result; //indexOf had valid result
        }
    }

    //provides index of entry for calculation baseline
    public int getIndexOfBaseline(boolean alertMode) {
        int index = 0;
        if (alertMode) {
            index = getIndexOfAlert(0);
        } else {
            index = getIndexOfTakeoff(0);
        }
        return index;
    }

    // Return name of the row
    public String getRowName(int index) {
        return rowNames.get(index);
    }

    // Return true if the row is a positive delta time (occurs after show time)
    public boolean isRowAfterShow(int index) {
        return rowValues.get(index)[AFTERINDEX] > 0 ? true : false;
    }

    //return hour of the row
    public int getRowHours(int index) {
        return rowValues.get(index)[HOURINDEX];
    }

    //return minute of the row
    public int getRowMinutes(int index) {
        return rowValues.get(index)[MINUTEINDEX];
    }

    // Return row value (delta t) with date of baseTime
    public Calendar getRowDeltaTime(Calendar baseTime, int index) {
        baseTime.set(Calendar.HOUR_OF_DAY, rowValues.get(index)[HOURINDEX]);
        baseTime.set(Calendar.MINUTE, rowValues.get(index)[MINUTEINDEX]);
        return baseTime;
    }


    public String getRowDeltaTimeString(int index, boolean alertMode) {
        int correctedMinutes = (rowValues.get(index)[AFTERINDEX]*rowValues.get(index)[MINUTEINDEX]) - (rowValues.get(getIndexOfBaseline(alertMode))[AFTERINDEX] * rowValues.get(getIndexOfBaseline(alertMode))[MINUTEINDEX]);
        int correctedHours = (rowValues.get(index)[AFTERINDEX]*rowValues.get(index)[HOURINDEX]) - (rowValues.get(getIndexOfBaseline(alertMode))[AFTERINDEX] * rowValues.get(getIndexOfBaseline(alertMode))[HOURINDEX]);

        String result = "";

        if(correctedMinutes>=60){
            correctedHours += Math.floor(correctedMinutes/60);
        }else if(correctedMinutes<=-60){
            correctedHours -= Math.floor(Math.abs(correctedMinutes)/60);
        }
        correctedMinutes = correctedMinutes%60;
        if(correctedMinutes<0 && correctedHours >0){
            correctedHours -=1;
            correctedMinutes += 60;
        }

        if (correctedHours <0 || (correctedHours == 0 && correctedMinutes<0)) {
            result += "-";
        } else {
            result += "+";
        }
        result += Math.abs(correctedHours) + "+" + (Math.abs(correctedMinutes)<10?"0":"") + Math.abs(correctedMinutes);
        return result;
    }

    // Return time resulting from adding(or subtracting) row value (corrected to mode) and baseTime
    public Calendar getRowTime(Calendar baseTime, int index, boolean alertMode) {
        Calendar output = (Calendar) baseTime.clone();
        output.add(
                Calendar.HOUR_OF_DAY,
                (rowValues.get(index)[AFTERINDEX] * rowValues.get(index)[HOURINDEX]) - (rowValues.get(getIndexOfBaseline(alertMode))[AFTERINDEX] * rowValues.get(getIndexOfBaseline(alertMode))[HOURINDEX]));
        output.add(
                Calendar.MINUTE,
                (rowValues.get(index)[AFTERINDEX] * rowValues.get(index)[MINUTEINDEX]) - (rowValues.get(getIndexOfBaseline(alertMode))[AFTERINDEX] * rowValues.get(getIndexOfBaseline(alertMode))[MINUTEINDEX]));
        return output;
    }

    // Create a profile class from file containing required data
    public ProfileClass(DataInputStream in) throws Exception {
        try {
            String input = in.readUTF();
            if (input.equals("--NEW PROFILE--")) {
                name = in.readUTF();// profile name

                Log.d("profile IN", name);
                while (true) {// loop until end of file
                    String rowName = in.readUTF();
                    if (rowName.equals("--END PROFILE--")) {
                        break;// don't continue through more profiles
                    }
                    int after = in.readInt();
                    int hour = in.readInt();
                    int minute = in.readInt();
                    addRow(rowName, (after == 1), hour, minute);
                }
                initializeProfile();
            } else {
                throw new Exception("no profile found");
            }
        } catch (EOFException e) {
            Log.i("Profile IN err", "End of file reached");
            throw new Exception("no profile found");
        } catch (Exception e) {
            Log.d("Profile IN err", "input " + e.toString());
            throw new Exception("error importing profile: " + e.toString());
        }
    }

    // add profile to file
    public void exportToFile(DataOutputStream out) {
        try {
            // generate output in file for the profile
            out.writeUTF("--NEW PROFILE--");
            out.writeUTF(name);
            for (int i = 0; i < rowNames.size(); i++) {

                out.writeUTF(rowNames.get(i));
                out.writeInt(rowValues.get(i)[AFTERINDEX]);
                out.writeInt(rowValues.get(i)[HOURINDEX]);
                out.writeInt(rowValues.get(i)[MINUTEINDEX]);

            }
            out.writeUTF("--END PROFILE--");
            Log.d("OUTPUT", "print to file complete for profile " + name);
        } catch (Exception e) {
            // catch file errors
            Log.d("Profile Out err", "output " + e.toString());
        }

    }

    // Create a profile class from user readable input
    // readable param is only there to differentiate from non-readable
    // constructor
    public ProfileClass(DataInputStream in, boolean readable) throws Exception {
        try {
            String input = in.readLine();
            if (input.equals("--NEW PROFILE--")) {
                name = in.readLine().substring(14);// profile name without
                // readable prefix

                Log.d("profile IN", name);
                in.readLine();// trash formatting line
                while (true) {// loop until end of file
                    String row = in.readLine();
                    if (row.equals("--END PROFILE--")) {
                        break;// don't continue through more profiles
                    }
                    String[] rowInfo = row.split("\\|");
                    int after = Integer.parseInt(rowInfo[1]);
                    int hour = Integer.parseInt(rowInfo[2]);
                    int minute = Integer.parseInt(rowInfo[3]);
                    addRow(rowInfo[0], (after == 1), hour, minute);
                }
                initializeProfile();
            } else {
                throw new Exception("no profile found");
            }
        } catch (EOFException e) {
            Log.i("Profile In", "End of file reached");
            throw new Exception("no profile found");
        } catch (Exception e) {
            Log.d("Profile IN err", "input " + e.toString());
            throw new Exception("error importing profile: " + e.toString());
        }
    }

    // generate user-readable file
    public void exportToReadableFile(DataOutputStream out) {
        try {
            // generate output in file for the profile
            out.write(("--NEW PROFILE--" + '\n').getBytes());
            out.write(("Profile Name: " + name + '\n').getBytes());
            out.write(("Row Name|afterAlert=1 before=-1|hours|minutes" + '\n')
                    .getBytes());// format explanation line
            for (int i = 0; i < rowNames.size(); i++) {

                out.write((rowNames.get(i) + '|'
                        + rowValues.get(i)[AFTERINDEX] + '|'
                        + rowValues.get(i)[HOURINDEX] + '|'
                        + rowValues.get(i)[MINUTEINDEX] + '\n').getBytes());

            }
            out.write(("--END PROFILE--" + '\n').getBytes());
            Log.d("OUTPUT", "print to readable complete for profile " + name);
        } catch (Exception e) {
            // catch file errors
            Log.d("Profile Out err", "output " + e.toString());
        }

    }

}
