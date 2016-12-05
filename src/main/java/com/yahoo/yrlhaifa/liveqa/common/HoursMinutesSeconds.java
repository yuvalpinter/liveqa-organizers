// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.common;


/**
 * Encapsulates hours, minutes and seconds.
 *
 * Date: Jan 15, 2015
 * 
 * @author Asher Stern
 *
 */
public class HoursMinutesSeconds {
    public HoursMinutesSeconds(int hours, int minutes, int seconds) {
        super();
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }



    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    @Override
    public String toString() {
        try {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return HoursMinutesSeconds.class.getName() + " (fields could not be printed)";

        }
    }



    private final int hours;
    private final int minutes;
    private final int seconds;
}
