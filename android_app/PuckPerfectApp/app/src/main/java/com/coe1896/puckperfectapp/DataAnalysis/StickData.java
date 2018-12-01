package com.coe1896.puckperfectapp.DataAnalysis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class StickData implements PuckPerfectData {

    Calendar cal;
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public ArrayList<String> times = new ArrayList<>();
    public ArrayList<Short> distances = new ArrayList<>();

    public void storeData(String input)
    {
        cal = new GregorianCalendar(TimeZone.getTimeZone("EST"));
        fmt.setCalendar(cal);
        String currTime = fmt.format(cal.getTime());

        String inputSplit[] = input.split(" ");
        short distance = (short) 0;

        if(inputSplit.length > 0 && !inputSplit[0].equals(""))
            distance = Short.parseShort(inputSplit[0]);

        times.add(currTime);
        distances.add(distance);
    }

    public String printLastDataPoint()
    {
        String output = "Time: " + times.get(times.size()-1) + "\n" +
                "Distance: " + distances.get(distances.size()-1) + "\n\n";

        return output;
    }
}
