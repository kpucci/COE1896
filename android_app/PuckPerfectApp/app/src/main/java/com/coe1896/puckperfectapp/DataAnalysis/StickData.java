package com.coe1896.puckperfectapp.DataAnalysis;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.coe1896.puckperfectapp.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class StickData implements PuckPerfectData {
    public final String TAG = "STICK DATA";

    Calendar cal;
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public ArrayList<String> times = new ArrayList<>();
    public ArrayList<Short> distances = new ArrayList<>();

    public void storeData(String input)
    {
        cal = new GregorianCalendar(TimeZone.getTimeZone("EST"));
        fmt.setCalendar(cal);
        String currTime = fmt.format(cal.getTime());

        String inputSplit[] = input.split("\n");
        short distance = (short) 0;


        try
        {
            if(inputSplit.length > 0 && !inputSplit[0].equals(""))
                distance = Short.parseShort(inputSplit[0]);

            times.add(currTime);
            distances.add(distance);
        }
        catch (NumberFormatException e)
        {
            Log.i(TAG, "STICK: Parse Error");
            e.printStackTrace();
        }

    }

    public void storeData(byte[] buffer)
    {
        cal = new GregorianCalendar(TimeZone.getTimeZone("EST"));
        fmt.setCalendar(cal);
        String currTime = fmt.format(cal.getTime());

        short distance;

        try
        {
            distance = Short.parseShort((new String(buffer, 0, 3)).trim());

            times.add(currTime);
            distances.add(distance);
        }
        catch (NumberFormatException e)
        {
            Log.i(TAG, "STICK: Parse Error");
            e.printStackTrace();
        }

    }

    public String printLastDataPoint()
    {
        String output = "Time: " + times.get(times.size()-1) + "\n" +
                "Distance: " + distances.get(distances.size()-1) + "\n\n";

        return output;
    }

    public void exportToFile(Context context)
    {
        Log.i(TAG, "----- EXPORT STICK DATA -----");

        File path = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "/PuckPerfect");

        if (!path.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }

        File file = new File(path, "stick_data.txt");

        Log.i(TAG, file.getAbsolutePath());

        FileOutputStream stream;
        try
        {
            stream = new FileOutputStream(file);

            String colTitles = "Times\tDistances\n";
            stream.write(colTitles.getBytes());

            String data;
            for(int i = 0; i < times.size(); i++)
            {
                data = String.format("%s\t%d\n", times.get(i), distances.get(i));
                stream.write(data.getBytes());
            }

            stream.close();
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void clear()
    {
        this.times = new ArrayList<>();
        this.distances = new ArrayList<>();
    }

    public boolean isEmpty()
    {
        return times.isEmpty();
    }
}
