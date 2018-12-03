package com.coe1896.puckperfectapp.DataAnalysis;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ConeData implements PuckPerfectData{

    public final String TAG = "CONE DATA";

    Calendar cal;
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public ArrayList<String> times = new ArrayList<>();
    public ArrayList<Integer> distC1 = new ArrayList<>();
    public ArrayList<Integer> distC2 = new ArrayList<>();
    public ArrayList<Integer> distC3 = new ArrayList<>();
    public ArrayList<Integer> distC4 = new ArrayList<>();
    public ArrayList<Integer> distC5 = new ArrayList<>();
    public ArrayList<Integer> distC6 = new ArrayList<>();
    public ArrayList<Integer> distC7 = new ArrayList<>();

    public void storeData(String input)
    {
        cal = new GregorianCalendar(TimeZone.getTimeZone("EST"));
        fmt.setCalendar(cal);
        String currTime = fmt.format(cal.getTime());

        int d1 = 0, d2 = 0, d3 = 0, d4 = 0,
                d5 = 0, d6 = 0, d7 = 0;

        String[] inputSplitNL, inputSplitSp;

        if(input.contains("\n"))
        {
            inputSplitNL = input.split("\n");

            if(inputSplitNL.length > 0) {
                inputSplitSp = inputSplitNL[0].split(" ");

                try
                {
                    if (inputSplitSp.length == 7) {
                        if (!inputSplitSp[0].equals(""))
                            d1 = Integer.parseInt(inputSplitSp[0]);
                        if (!inputSplitSp[1].equals(""))
                            d2 = Integer.parseInt(inputSplitSp[1]);
                        if (!inputSplitSp[2].equals(""))
                            d3 = Integer.parseInt(inputSplitSp[2]);
                        if (!inputSplitSp[3].equals(""))
                            d4 = Integer.parseInt(inputSplitSp[3]);
                        if (!inputSplitSp[4].equals(""))
                            d5 = Integer.parseInt(inputSplitSp[4]);
                        if (!inputSplitSp[5].equals(""))
                            d6 = Integer.parseInt(inputSplitSp[5]);
                        if (!inputSplitSp[6].equals(""))
                            d7 = Integer.parseInt(inputSplitSp[6]);
                    }

                    // TODO: Detect when all are 0 vs all except 1 are zero
                    times.add(currTime);
                    distC1.add(d1);
                    distC2.add(d2);
                    distC3.add(d3);
                    distC4.add(d4);
                    distC5.add(d5);
                    distC6.add(d6);
                    distC7.add(d7);
                }
                catch (NumberFormatException e)
                {
                    Log.i(TAG, "CONE: Parse Error");
                    e.printStackTrace();
                }
            }
        }
    }

    public String printLastDataPoint()
    {
        String output = "";

        if(times.size() > 0)
            output = "Time: " + times.get(times.size()-1) + "\n" +
                "Cone 1: " + distC1.get(distC1.size()-1) + "\n" +
                "Cone 2: " + distC2.get(distC2.size()-1) + "\n" +
                "Cone 3: " + distC3.get(distC3.size()-1) + "\n" +
                "Cone 4: " + distC4.get(distC4.size()-1) + "\n" +
                "Cone 5: " + distC5.get(distC5.size()-1) + "\n" +
                "Cone 6: " + distC6.get(distC6.size()-1) + "\n" +
                "Cone 7: " + distC7.get(distC7.size()-1) + "\n\n";

        return output;
    }

    public void exportToFile(Context context)
    {
        Log.i(TAG, "----- EXPORT CONE DATA -----");
        File path = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "/PuckPerfect");

        if (!path.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }

        File file = new File(path, "cone_data.txt");

        FileOutputStream stream;
        try
        {
            stream = new FileOutputStream(file);

            String colTitles = "Times\tC1\tC2\tC3\tC4\tC5\tC6\tC7\n";
            stream.write(colTitles.getBytes());

            String data;
            for(int i = 0; i < times.size(); i++)
            {
                data = String.format("%s %d %d %d %d %d %d %d\n", times.get(i), distC1.get(i), distC2.get(i), distC3.get(i), distC4.get(i), distC5.get(i),
                        distC6.get(i), distC7.get(i));
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
        this.distC1 = new ArrayList<>();
        this.distC2 = new ArrayList<>();
        this.distC3 = new ArrayList<>();
        this.distC4 = new ArrayList<>();
        this.distC5 = new ArrayList<>();
        this.distC6 = new ArrayList<>();
        this.distC7 = new ArrayList<>();
    }

    public boolean isEmpty()
    {
        return times.isEmpty();
    }
}

