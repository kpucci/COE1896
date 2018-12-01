package com.coe1896.puckperfectapp.DataAnalysis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class GloveData implements PuckPerfectData{

    Calendar cal;
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public ArrayList<String> times = new ArrayList<>();
    public ArrayList<Integer> pressureVals = new ArrayList<>();
    public ArrayList<Float> pitchVals = new ArrayList<>();
    public ArrayList<Float> yawVals = new ArrayList<>();
    public ArrayList<Float> rollVals = new ArrayList<>();

    public void storeData(String input)
    {
        cal = new GregorianCalendar(TimeZone.getTimeZone("EST"));
        fmt.setCalendar(cal);
        String currTime = fmt.format(cal.getTime());

        String inputSplit[] = input.split(" ");
        float yaw = (float) 0.0, pitch = (float) 0.0, roll = (float) 0.0;
        int pressure = 0;

        if(inputSplit.length > 3)
        {
            if(!inputSplit[0].equals(""))
                yaw = Float.parseFloat(inputSplit[0]);
            if(!inputSplit[1].equals(""))
                pitch = Float.parseFloat(inputSplit[1]);
            if(!inputSplit[2].equals(""))
                roll = Float.parseFloat(inputSplit[2]);
            if(!inputSplit[3].equals(""))
                pressure = Integer.parseInt(inputSplit[3]);
        }

        times.add(currTime);
        yawVals.add(yaw);
        pitchVals.add(pitch);
        rollVals.add(roll);
        pressureVals.add(pressure);
    }

    public String printLastDataPoint()
    {
        String output = "Time: " + times.get(times.size()-1) + "\n" +
                        "Yaw: " + Float.toString(yawVals.get(yawVals.size()-1)) + "\n" +
                        "Pitch: " + Float.toString(pitchVals.get(pitchVals.size()-1)) + "\n" +
                        "Roll: " + Float.toString(rollVals.get(rollVals.size()-1)) + "\n" +
                        "Pressure: " + pressureVals.get(pressureVals.size()-1) + "\n\n";

        return output;

    }

}
