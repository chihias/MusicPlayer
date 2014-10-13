
package com.example.musicplayer;

public class Utilities {
    public String milliSecondsToTimer(long miliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hrs = (int) (miliseconds / (1000 * 60 * 60));
        int mins = (int) (miliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int secs = (int) ((miliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hrs > 0) {
            finalTimerString = hrs + ":";
        }

        if (secs < 10) {
            secondsString = "0" + secs;
        } else {
            secondsString = " " + secs;
        }
        finalTimerString = finalTimerString + mins + ":" + secondsString;

        return finalTimerString;
    }

    public int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        long currentSecs = (int) (currentDuration / 1000);
        long totalSecs = (int) (totalDuration / 1000);
        percentage = (((double) currentSecs) / totalSecs) * 100;

        return percentage.intValue();
    }

    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;

        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        return currentDuration * 1000;
    }
}
