package com.karson.android.dutydaycalculator;

import android.icu.util.TimeZone;

import java.util.Date;

public class TimezoneEntry {
    public int currentOffset = 0;
    public String id;
    public String name;
    public int offsetHours;
    public int offsetMinutes;

    public TimezoneEntry(TimeZone tz, Date currentDate) {
        this.currentOffset = tz.getOffset(currentDate.getTime());
        this.id = tz.getID();
        this.name = (this.currentOffset == 0 ? "ZULU" : tz.getDisplayName());
        this.offsetHours = Math.round(this.currentOffset / (60 * 60000));
        this.offsetMinutes = Math.abs(Math.round((this.currentOffset - (this.offsetHours * 60 * 60000)) / 60000));
    }

    public int compareTo(TimezoneEntry compareTZ) {
        float comparision = (this.currentOffset - compareTZ.currentOffset);
        if (comparision == 0) {
            return 0;
        } else if (comparision > 0) {
            return 1;
        } else {
            return -1;
        }
    }
}
