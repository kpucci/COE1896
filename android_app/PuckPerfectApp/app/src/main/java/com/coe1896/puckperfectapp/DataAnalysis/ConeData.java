package com.coe1896.puckperfectapp.DataAnalysis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ConeData implements PuckPerfectData{

    Calendar cal;
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public ArrayList<String> times = new ArrayList<>();
    public ArrayList<Short> distC1 = new ArrayList<>();
    public ArrayList<Short> distC2 = new ArrayList<>();
    public ArrayList<Short> distC3 = new ArrayList<>();
    public ArrayList<Short> distC4 = new ArrayList<>();
    public ArrayList<Short> distC5 = new ArrayList<>();
    public ArrayList<Short> distC6 = new ArrayList<>();
    public ArrayList<Short> distC7 = new ArrayList<>();

    public void storeData(String input)
    {
        cal = new GregorianCalendar(TimeZone.getTimeZone("EST"));
        fmt.setCalendar(cal);
        String currTime = fmt.format(cal.getTime());

        String inputSplit[] = input.split(" ");
        short d1 = (short) 0, d2 = (short) 0, d3 = (short) 0, d4 = (short) 0,
                d5 = (short) 0, d6 = (short) 0, d7 = (short) 0;

        if(inputSplit.length > 6)
        {
            if(!inputSplit[0].equals(""))
                d1 = Short.parseShort(inputSplit[0]);
            if(!inputSplit[1].equals(""))
                d2 = Short.parseShort(inputSplit[1]);
            if(!inputSplit[2].equals(""))
                d3 = Short.parseShort(inputSplit[2]);
            if(!inputSplit[3].equals(""))
                d4 = Short.parseShort(inputSplit[3]);
            if(!inputSplit[4].equals(""))
                d5 = Short.parseShort(inputSplit[4]);
            if(!inputSplit[5].equals(""))
                d6 = Short.parseShort(inputSplit[5]);
            if(!inputSplit[6].equals(""))
                d7 = Short.parseShort(inputSplit[6]);
        }

        times.add(currTime);
        distC1.add(d1);
        distC2.add(d2);
        distC3.add(d3);
        distC4.add(d4);
        distC5.add(d5);
        distC6.add(d6);
        distC7.add(d7);
    }

    public String printLastDataPoint()
    {
        String output = "Time: " + times.get(times.size()-1) + "\n" +
                "Cone 1: " + distC1.get(distC1.size()-1) + "\n" +
                "Cone 2: " + distC2.get(distC2.size()-1) + "\n" +
                "Cone 3: " + distC3.get(distC3.size()-1) + "\n" +
                "Cone 4: " + distC4.get(distC4.size()-1) + "\n" +
                "Cone 5: " + distC5.get(distC5.size()-1) + "\n" +
                "Cone 6: " + distC6.get(distC6.size()-1) + "\n" +
                "Cone 7: " + distC7.get(distC7.size()-1) + "\n\n";

        return output;
    }
}
