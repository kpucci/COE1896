package com.coe1896.puckperfectapp.DataAnalysis;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.coe1896.puckperfectapp.PuckPerfectDevice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class GloveData implements PuckPerfectData{
    public final String TAG = "GLOVE DATA";

    Calendar cal;
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public ArrayList<String> times = new ArrayList<>();
    public ArrayList<Float> pitchVals = new ArrayList<>();
    public ArrayList<Float> yawVals = new ArrayList<>();
    public ArrayList<Float> rollVals = new ArrayList<>();
    public ArrayList<Integer> pressureVals = new ArrayList<>();

    private PuckPerfectDevice.DEVICE_TYPE type;

    public GloveData(PuckPerfectDevice.DEVICE_TYPE type)
    {
        this.type = type;
    }

    public void storeData(String input)
    {
        cal = new GregorianCalendar(TimeZone.getTimeZone("EST"));
        fmt.setCalendar(cal);
        String currTime = fmt.format(cal.getTime());

        float yaw = (float) 0.0, pitch = (float) 0.0, roll = (float) 0.0;
        int pressure = 0;

        String[] inputSplitNL, inputSplitSp;

        if(input.contains("\n")) {
            inputSplitNL = input.split("\n");

            if (inputSplitNL.length > 0) {
                inputSplitSp = inputSplitNL[0].split(" ");

                try
                {
                    if (inputSplitSp.length > 3) {
                        if (!inputSplitSp[0].equals(""))
                            yaw = Float.parseFloat(inputSplitSp[0]);
                        if (!inputSplitSp[1].equals(""))
                            pitch = Float.parseFloat(inputSplitSp[1]);
                        if (!inputSplitSp[2].equals(""))
                            roll = Float.parseFloat(inputSplitSp[2]);
                        if (!inputSplitSp[3].equals(""))
                            pressure = Integer.parseInt(inputSplitSp[3]);
                    }

                    if(yaw != 0 || pitch != 0 || roll != 0)
                    {
                        times.add(currTime);
                        yawVals.add(yaw);
                        pitchVals.add(pitch);
                        rollVals.add(roll);
                        pressureVals.add(pressure);
                    }
                }
                catch (NumberFormatException e)
                {
                    Log.i(TAG, "GLOVE: Parse Error");
                    e.printStackTrace();
                }
            }
        }
    }

    public void storeData(byte[] buffer)
    {
        cal = new GregorianCalendar(TimeZone.getTimeZone("EST"));
        fmt.setCalendar(cal);
        String currTime = fmt.format(cal.getTime());

        float yaw, pitch, roll;
        int pressure;

        try
        {
            // 0-7
            Log.i(TAG, "1: " + (new String(buffer, 0, 8)).trim());
            yaw = Float.parseFloat((new String(buffer, 0, 8)).trim());

            // 8-
            Log.i(TAG, "2: " + (new String(buffer, 8, 8)).trim());
            pitch = Float.parseFloat((new String(buffer, 8, 8)).trim());

            Log.i(TAG, "3: " + (new String(buffer, 16, 8)).trim());
            roll = Float.parseFloat((new String(buffer, 16, 8)).trim());

            Log.i(TAG, "4: " + (new String(buffer, 24, 5)).trim());
            pressure = Integer.parseInt((new String(buffer, 24, 5)).trim());

            if(yaw != 0 && pitch != 0 && roll != 0)
            {
                times.add(currTime);
                yawVals.add(yaw);
                pitchVals.add(pitch);
                rollVals.add(roll);
                pressureVals.add(pressure);
            }
        }
        catch (NumberFormatException e)
        {
            Log.i(TAG, "GLOVE: Parse Error");
            e.printStackTrace();
        }
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

    public void exportToFile(Context context)
    {
        Log.i(TAG, "----- EXPORT GLOVE DATA -----");

        File path = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "/PuckPerfect");

        if (!path.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }

        File file = null;
        if(this.type == PuckPerfectDevice.DEVICE_TYPE.L_GLOVE)
            file = new File(path, "l_glove_data.txt");
        else if(this.type == PuckPerfectDevice.DEVICE_TYPE.R_GLOVE)
            file = new File(path, "r_glove_data.txt");

        if(file != null)
        {
            FileOutputStream stream;
            try
            {
                stream = new FileOutputStream(file);

                String colTitles = "Times\tYaw\tPitch\tRoll\tPressure\n";
                stream.write(colTitles.getBytes());

                String data;
                for(int i = 0; i < times.size(); i++)
                {
                    data = String.format("%s %4.2f %4.2f %4.2f %d\n", times.get(i), yawVals.get(i), pitchVals.get(i), rollVals.get(i), pressureVals.get(i));
                    stream.write(data.getBytes());
                }

                stream.close();
            }
            catch (IOException e)
            {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }

    }

    public void clear()
    {
        this.times = new ArrayList<>();
        this.pitchVals = new ArrayList<>();
        this.yawVals = new ArrayList<>();
        this.rollVals = new ArrayList<>();
        this.pressureVals = new ArrayList<>();
    }

    public boolean isEmpty()
    {
        return times.isEmpty();
    }

}
