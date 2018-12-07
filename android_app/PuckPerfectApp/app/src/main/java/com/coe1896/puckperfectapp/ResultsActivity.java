package com.coe1896.puckperfectapp;

import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity {

    private static final String TAG = "ResultsActivity";

    private ArrayList<String> rTimes = new ArrayList<>();
    private ArrayList<DataPoint> rYawPoints = new ArrayList<>();
    private ArrayList<DataPoint> rPitchPoints = new ArrayList<>();
    private ArrayList<DataPoint> rRollPoints = new ArrayList<>();

    private ArrayList<DataPoint> rYawExpert = new ArrayList<>();
    private ArrayList<DataPoint> rPitchExpert = new ArrayList<>();
    private ArrayList<DataPoint> rRollExpert = new ArrayList<>();

    private ArrayList<String> lTimes = new ArrayList<>();
    private ArrayList<DataPoint> lYawPoints = new ArrayList<>();
    private ArrayList<DataPoint> lPitchPoints = new ArrayList<>();
    private ArrayList<DataPoint> lRollPoints = new ArrayList<>();

    private ArrayList<DataPoint> lYawExpert = new ArrayList<>();
    private ArrayList<DataPoint> lPitchExpert = new ArrayList<>();
    private ArrayList<DataPoint> lRollExpert = new ArrayList<>();

    private ArrayList<String> stickTimes = new ArrayList<>();
    private ArrayList<DataPoint> stickPoints = new ArrayList<>();

    private ArrayList<String> cTimes = new ArrayList<>();
    private ArrayList<DataPoint> c1Points = new ArrayList<>();
    private ArrayList<DataPoint> c2Points = new ArrayList<>();
    private ArrayList<DataPoint> c3Points = new ArrayList<>();
    private ArrayList<DataPoint> c4Points = new ArrayList<>();
    private ArrayList<DataPoint> c5Points = new ArrayList<>();
    private ArrayList<DataPoint> c6Points = new ArrayList<>();
    private ArrayList<DataPoint> c7Points = new ArrayList<>();

    GraphView rightGraph;
    GraphView leftGraph;
    GraphView rightExpertGraph;
    GraphView leftExpertGraph;

    TextView recommendationText;

    private double[] leftYawExpAvgs = {113.78, 130.90, 77.88, 83.91, 98.01, 96.20, 128.03};
    private double[] leftPitchExpAvgs = {58.83, 36.30, 92.73, 129.98, 44.81, 55.11, 31.83};
    private double[] leftRollExpAvgs = {68.10, 51.90, 103.52, 148.04, 60.51, 57.14, 43.77};
    private double[] rightYawExpAvgs = {59.33, 66.30, 69.51, 67.26, 31.80, 40.69, 49.94};
    private double[] rightPitchExpAvgs = {130.66, 131.6, 99.20, 112.20, 138.71, 138.61, 140.21};
    private double[] rightRollExpAvgs = {126.80, 108.96, 94.40, 132.95, 114.34, 110.40, 127.55};

    private final int stickExpAvg = 207; // 311.7 103.9

    private int[] coneExpAvgs = {15, 20, 15, 25, 12, 22, 16};

    double[] leftYawError = {0, 0, 0, 0, 0, 0, 0};
    double[] leftYawScore = {0, 0, 0, 0, 0, 0, 0};
    double[] leftPitchError = {0, 0, 0, 0, 0, 0, 0};
    double[] leftPitchScore = {0, 0, 0, 0, 0, 0, 0};
    double[] leftRollError = {0, 0, 0, 0, 0, 0, 0};
    double[] leftRollScore = {0, 0, 0, 0, 0, 0, 0};

    double[] rightYawError = {0, 0, 0, 0, 0, 0, 0};
    double[] rightYawScore = {0, 0, 0, 0, 0, 0, 0};
    double[] rightPitchError = {0, 0, 0, 0, 0, 0, 0};
    double[] rightPitchScore = {0, 0, 0, 0, 0, 0, 0};
    double[] rightRollError = {0, 0, 0, 0, 0, 0, 0};
    double[] rightRollScore = {0, 0, 0, 0, 0, 0, 0};

    double[] coneError = {0, 0, 0, 0, 0, 0, 0};
    double[] coneScore = {0, 0, 0, 0, 0, 0, 0};

    double stickError = 0.0;
    double stickScore = 0.0;

    boolean calcStick = false;
    boolean calcRightHand = false;
    boolean calcCones = true;
    boolean calcLeftHand = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        rightGraph = (GraphView) findViewById(R.id.rightGraph);
        leftGraph = (GraphView) findViewById(R.id.leftGraph);

        rightExpertGraph = (GraphView) findViewById(R.id.rightExpertGraph);
        leftExpertGraph = (GraphView) findViewById(R.id.leftExpertGraph);

        recommendationText = (TextView) findViewById(R.id.recommendationText);

        getRightGloveData();
        populateRightGraph();

        getLeftGloveData();
        populateLeftGraph();

        getRightGloveExpert();
        populateRightExpertGraph();

        getLeftGloveExpert();
        populateLeftExpertGraph();

        getConeData();
        getStickData();
        calculateErrors();
        giveRecommendations();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Log.i(TAG, "----- START RESULTS ACTIVITY -----");
    }

    @Override
    protected void onDestroy()
    {
        Log.i(TAG, "----- DESTROY RESULTS ACTIVITY -----");
        super.onDestroy();
    }

    public void getConeData()
    {
        // Open file and get data points
        File path = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "/PuckPerfect");

        File file = new File(path, "cone_data.txt");
        FileInputStream stream;
        BufferedReader reader;
        try
        {
            stream = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(stream));
            String line = reader.readLine(); // absorb column titles
            int i = 0;
            String[] lineSplit;
            while(line != null)
            {
                Log.i(TAG, line);
                lineSplit = line.split(" ");
                if(lineSplit.length == 8)
                {
                    cTimes.add(lineSplit[0]);
                    c1Points.add(new DataPoint(i, Integer.parseInt(lineSplit[1])));
                    c2Points.add(new DataPoint(i, Integer.parseInt(lineSplit[2])));
                    c3Points.add(new DataPoint(i, Integer.parseInt(lineSplit[3])));
                    c4Points.add(new DataPoint(i, Integer.parseInt(lineSplit[4])));
                    c5Points.add(new DataPoint(i, Integer.parseInt(lineSplit[5])));
                    c6Points.add(new DataPoint(i, Integer.parseInt(lineSplit[6])));
                    c7Points.add(new DataPoint(i, Integer.parseInt(lineSplit[7])));
                }

                line = reader.readLine();
                i++;
            }
        }
        catch(IOException e)
        {
            Log.i(TAG, "Couldn't open file");
        }
    }

    public void getRightGloveData()
    {
        Log.i(TAG, "----- GET RIGHT GLOVE DATA -----");
        // Open file and get data points
        File path = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "/PuckPerfect");

        File file = new File(path, "r_glove_data.txt");
        FileInputStream stream;
        BufferedReader reader;
        try
        {
            stream = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(stream));
            String line = reader.readLine(); // absorb column titles
            int i = 0;
            String[] lineSplit;
            while(line != null)
            {
                Log.i(TAG, line);
                lineSplit = line.split(" ");
                if(lineSplit.length >= 4)
                {
                    rTimes.add(lineSplit[0]);
                    rYawPoints.add(new DataPoint(i, Float.parseFloat(lineSplit[1])));
                    rPitchPoints.add(new DataPoint(i, Float.parseFloat(lineSplit[2])));
                    rRollPoints.add(new DataPoint(i, Float.parseFloat(lineSplit[3])));
                }

                line = reader.readLine();
                i++;
            }
        }
        catch(IOException e)
        {
            Log.i(TAG, "Couldn't open file");
        }
    }

    public void getLeftGloveData()
    {
        Log.i(TAG, "----- GET LEFT GLOVE DATA -----");
        // Open file and get data points
        File path = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "/PuckPerfect");

        File file = new File(path, "l_glove_data.txt");
        FileInputStream stream;
        BufferedReader reader;
        try
        {
            stream = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(stream));
            String line = reader.readLine(); // absorb column titles
            int i = 0;
            String[] lineSplit;
            while(line != null)
            {
                lineSplit = line.split(" ");
                if(lineSplit.length >= 4)
                {
                    lTimes.add(lineSplit[0]);
                    lYawPoints.add(new DataPoint(i, Float.parseFloat(lineSplit[1])));
                    lPitchPoints.add(new DataPoint(i, Float.parseFloat(lineSplit[2])));
                    lRollPoints.add(new DataPoint(i, Float.parseFloat(lineSplit[3])));
                }

                line = reader.readLine();
                i++;
            }
        }
        catch(IOException e)
        {

        }
    }

    public void getRightGloveExpert()
    {
        Log.i(TAG, "----- GET RIGHT GLOVE DATA -----");
        // Open file and get data points
        File path = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "/PuckPerfect");

        File file = new File(path, "r_glove_expert.txt");
        FileInputStream stream;
        BufferedReader reader;
        try
        {
            stream = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(stream));
            String line = reader.readLine(); // absorb column titles
            int i = 0;
            String[] lineSplit;
            while(line != null)
            {
                Log.i(TAG, line);
                lineSplit = line.split(" ");
                if(lineSplit.length >= 4)
                {
                    rYawExpert.add(new DataPoint(i, Float.parseFloat(lineSplit[1])));
                    rPitchExpert.add(new DataPoint(i, Float.parseFloat(lineSplit[2])));
                    rRollExpert.add(new DataPoint(i, Float.parseFloat(lineSplit[3])));
                }

                line = reader.readLine();
                i++;
            }
        }
        catch(IOException e)
        {
            Log.i(TAG, "Couldn't open file");
        }
    }

    public void getLeftGloveExpert()
    {
        Log.i(TAG, "----- GET LEFT GLOVE DATA -----");
        // Open file and get data points
        File path = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "/PuckPerfect");

        File file = new File(path, "l_glove_expert.txt");
        FileInputStream stream;
        BufferedReader reader;
        try
        {
            stream = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(stream));
            String line = reader.readLine(); // absorb column titles
            int i = 0;
            String[] lineSplit;
            while(line != null)
            {
                lineSplit = line.split(" ");
                if(lineSplit.length >= 4)
                {
                    lYawExpert.add(new DataPoint(i, Float.parseFloat(lineSplit[1])));
                    lPitchExpert.add(new DataPoint(i, Float.parseFloat(lineSplit[2])));
                    lRollExpert.add(new DataPoint(i, Float.parseFloat(lineSplit[3])));
                }

                line = reader.readLine();
                i++;
            }
        }
        catch(IOException e)
        {

        }
    }

    public void getStickData()
    {
        // Open file and get data points
        File path = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "/PuckPerfect");

        File file = new File(path, "stick_data.txt");
        FileInputStream stream;
        BufferedReader reader;
        try
        {
            stream = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(stream));
            String line = reader.readLine(); // absorb column titles
            int i = 0;
            String[] lineSplit;
            while(line != null)
            {
                Log.i(TAG, line);
                lineSplit = line.split(" ");
                if(lineSplit.length >= 2)
                {
                    stickTimes.add(lineSplit[0]);
                    stickPoints.add(new DataPoint(i, Float.parseFloat(lineSplit[1])));
                }

                line = reader.readLine();
                i++;
            }
        }
        catch(IOException e)
        {

        }
    }

    public void populateRightGraph()
    {
        Log.i(TAG, "----- POPULATE RIGHT GRAPH -----");
        if(this.rYawPoints.size() != 0)
        {
            DataPoint[] rightYawPoints = new DataPoint[this.rYawPoints.size()];
            DataPoint[] rightPitchPoints = new DataPoint[this.rPitchPoints.size()];
            DataPoint[] rightRollPoints = new DataPoint[this.rRollPoints.size()];

            for(int i=0; i<this.rYawPoints.size(); i++)
            {
                rightYawPoints[i] = this.rYawPoints.get(i);
                rightPitchPoints[i] = this.rPitchPoints.get(i);
                rightRollPoints[i] = this.rRollPoints.get(i);
            }

            LineGraphSeries<DataPoint> yawSeries = new LineGraphSeries<>(rightYawPoints);
            yawSeries.setTitle("Yaw");
            yawSeries.setColor(Color.GREEN);
            yawSeries.setDrawDataPoints(true);
            yawSeries.setDataPointsRadius(8);
            yawSeries.setThickness(8);

            LineGraphSeries<DataPoint> pitchSeries = new LineGraphSeries<>(rightPitchPoints);
            pitchSeries.setTitle("Pitch");
            pitchSeries.setColor(Color.BLUE);
            pitchSeries.setDrawDataPoints(true);
            pitchSeries.setDataPointsRadius(8);
            pitchSeries.setThickness(8);

            LineGraphSeries<DataPoint> rollSeries = new LineGraphSeries<>(rightRollPoints);
            rollSeries.setTitle("Roll");
            rollSeries.setColor(Color.RED);
            rollSeries.setDrawDataPoints(true);
            rollSeries.setDataPointsRadius(8);
            rollSeries.setThickness(8);

            rightGraph.addSeries(yawSeries);
            rightGraph.addSeries(pitchSeries);
            rightGraph.addSeries(rollSeries);

            rightGraph.getLegendRenderer().setVisible(true);
            rightGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

            rightGraph.setTitle("Right Glove Results");
            rightGraph.setTitleTextSize(30);

            rightGraph.getViewport().setMinX(0);
            rightGraph.getViewport().setMaxX(rightYawPoints.length);
            rightGraph.getViewport().setXAxisBoundsManual(true);
        }
    }

    public void populateLeftGraph() {
        Log.i(TAG, "----- POPULATE LEFT GRAPH -----");
        if (this.lYawPoints.size() != 0) {
            DataPoint[] leftYawPoints = new DataPoint[this.lYawPoints.size()];
            DataPoint[] leftPitchPoints = new DataPoint[this.lPitchPoints.size()];
            DataPoint[] leftRollPoints = new DataPoint[this.lRollPoints.size()];

            for (int i = 0; i < this.lYawPoints.size(); i++) {
                leftYawPoints[i] = this.lYawPoints.get(i);
                leftPitchPoints[i] = this.lPitchPoints.get(i);
                leftRollPoints[i] = this.lRollPoints.get(i);
            }

            LineGraphSeries<DataPoint> yawSeries = new LineGraphSeries<>(leftYawPoints);
            yawSeries.setTitle("Yaw");
            yawSeries.setColor(Color.GREEN);
            yawSeries.setDrawDataPoints(true);
            yawSeries.setDataPointsRadius(5);
            yawSeries.setThickness(5);

            LineGraphSeries<DataPoint> pitchSeries = new LineGraphSeries<>(leftPitchPoints);
            pitchSeries.setTitle("Pitch");
            pitchSeries.setColor(Color.BLUE);
            pitchSeries.setDrawDataPoints(true);
            pitchSeries.setDataPointsRadius(5);
            pitchSeries.setThickness(5);

            LineGraphSeries<DataPoint> rollSeries = new LineGraphSeries<>(leftRollPoints);
            rollSeries.setTitle("Roll");
            rollSeries.setColor(Color.RED);
            rollSeries.setDrawDataPoints(true);
            rollSeries.setDataPointsRadius(5);
            rollSeries.setThickness(5);

            leftGraph.addSeries(yawSeries);
            leftGraph.addSeries(pitchSeries);
            leftGraph.addSeries(rollSeries);

            leftGraph.getLegendRenderer().setVisible(true);
            leftGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

            leftGraph.setTitle("Left Glove Results");
            leftGraph.setTitleTextSize(30);

            leftGraph.getViewport().setMinX(0);
            leftGraph.getViewport().setMaxX(leftYawPoints.length);
            leftGraph.getViewport().setXAxisBoundsManual(true);
        }
    }

    public void populateRightExpertGraph()
    {
        Log.i(TAG, "----- POPULATE RIGHT EXPERT GRAPH -----");
        if(this.rYawPoints.size() != 0)
        {
            DataPoint[] rightYawPoints = new DataPoint[this.rYawExpert.size()];
            DataPoint[] rightPitchPoints = new DataPoint[this.rPitchExpert.size()];
            DataPoint[] rightRollPoints = new DataPoint[this.rRollExpert.size()];

            for(int i=0; i<this.rYawExpert.size(); i++)
            {
                rightYawPoints[i] = this.rYawExpert.get(i);
                rightPitchPoints[i] = this.rPitchExpert.get(i);
                rightRollPoints[i] = this.rRollExpert.get(i);
            }

            LineGraphSeries<DataPoint> yawSeries = new LineGraphSeries<>(rightYawPoints);
            yawSeries.setTitle("Yaw");
            yawSeries.setColor(Color.GREEN);
            yawSeries.setDrawDataPoints(true);
            yawSeries.setDataPointsRadius(5);
            yawSeries.setThickness(5);

            LineGraphSeries<DataPoint> pitchSeries = new LineGraphSeries<>(rightPitchPoints);
            pitchSeries.setTitle("Pitch");
            pitchSeries.setColor(Color.BLUE);
            pitchSeries.setDrawDataPoints(true);
            pitchSeries.setDataPointsRadius(5);
            pitchSeries.setThickness(5);

            LineGraphSeries<DataPoint> rollSeries = new LineGraphSeries<>(rightRollPoints);
            rollSeries.setTitle("Roll");
            rollSeries.setColor(Color.RED);
            rollSeries.setDrawDataPoints(true);
            rollSeries.setDataPointsRadius(5);
            rollSeries.setThickness(5);

            rightExpertGraph.addSeries(yawSeries);
            rightExpertGraph.addSeries(pitchSeries);
            rightExpertGraph.addSeries(rollSeries);

            rightExpertGraph.getLegendRenderer().setVisible(true);
            rightExpertGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

            rightExpertGraph.setTitle("Right Glove Expert Results");
            rightExpertGraph.setTitleTextSize(30);

            rightExpertGraph.getViewport().setMinX(0);
            rightExpertGraph.getViewport().setMaxX(rightYawPoints.length);
            rightExpertGraph.getViewport().setXAxisBoundsManual(true);
        }
    }

    public void populateLeftExpertGraph() {
        Log.i(TAG, "----- POPULATE LEFT EXPERT GRAPH -----");
        if (this.lYawPoints.size() != 0) {
            DataPoint[] leftYawPoints = new DataPoint[this.lYawExpert.size()];
            DataPoint[] leftPitchPoints = new DataPoint[this.lPitchExpert.size()];
            DataPoint[] leftRollPoints = new DataPoint[this.lRollExpert.size()];

            for (int i = 0; i < this.lYawExpert.size(); i++) {
                leftYawPoints[i] = this.lYawExpert.get(i);
                leftPitchPoints[i] = this.lPitchExpert.get(i);
                leftRollPoints[i] = this.lRollExpert.get(i);
            }

            LineGraphSeries<DataPoint> yawSeries = new LineGraphSeries<>(leftYawPoints);
            yawSeries.setTitle("Yaw");
            yawSeries.setColor(Color.GREEN);
            yawSeries.setDrawDataPoints(true);
            yawSeries.setDataPointsRadius(5);
            yawSeries.setThickness(5);

            LineGraphSeries<DataPoint> pitchSeries = new LineGraphSeries<>(leftPitchPoints);
            pitchSeries.setTitle("Pitch");
            pitchSeries.setColor(Color.BLUE);
            pitchSeries.setDrawDataPoints(true);
            pitchSeries.setDataPointsRadius(5);
            pitchSeries.setThickness(5);

            LineGraphSeries<DataPoint> rollSeries = new LineGraphSeries<>(leftRollPoints);
            rollSeries.setTitle("Roll");
            rollSeries.setColor(Color.RED);
            rollSeries.setDrawDataPoints(true);
            rollSeries.setDataPointsRadius(5);
            rollSeries.setThickness(5);

            leftExpertGraph.addSeries(yawSeries);
            leftExpertGraph.addSeries(pitchSeries);
            leftExpertGraph.addSeries(rollSeries);

            leftExpertGraph.getLegendRenderer().setVisible(true);
            leftExpertGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

            leftExpertGraph.setTitle("Left Glove Expert Results");
            leftExpertGraph.setTitleTextSize(30);

            leftExpertGraph.getViewport().setMinX(0);
            leftExpertGraph.getViewport().setMaxX(leftYawPoints.length);
            leftExpertGraph.getViewport().setXAxisBoundsManual(true);
        }
    }

    public void calculateErrors()
    {
        // Glove recommendations
        double[] coneAvgs = {0, 0, 0, 0, 0, 0, 0};
        int[] coneNums = {0, 0, 0, 0, 0, 0, 0};

        double[] leftYawAvgs = {0, 0, 0, 0, 0, 0, 0};
        double[] leftPitchAvgs = {0, 0, 0, 0, 0, 0, 0};
        double[] leftRollAvgs = {0, 0, 0, 0, 0, 0, 0};

        int[] leftYawNums = {0, 0, 0, 0, 0, 0, 0};
        int[] leftPitchNums = {0, 0, 0, 0, 0, 0, 0};
        int[] leftRollNums = {0, 0, 0, 0, 0, 0, 0};

        double[] rightYawAvgs = {0, 0, 0, 0, 0, 0, 0};
        double[] rightPitchAvgs = {0, 0, 0, 0, 0, 0, 0};
        double[] rightRollAvgs = {0, 0, 0, 0, 0, 0, 0};

        int[] rightYawNums = {0, 0, 0, 0, 0, 0, 0};
        int[] rightPitchNums = {0, 0, 0, 0, 0, 0, 0};
        int[] rightRollNums = {0, 0, 0, 0, 0, 0, 0};


        // For each datapoint in each cone data, find where it isn't zero and average values
        double cVal;
        ArrayList<DataPoint> conePoints = null;
        for(int j=0; j < 7; j++)
        {
            if(j == 0)      conePoints = c1Points;
            else if(j == 1) conePoints = c2Points;
            else if(j == 2) conePoints = c3Points;
            else if(j == 3) conePoints = c4Points;
            else if(j == 4) conePoints = c5Points;
            else if(j == 5) conePoints = c6Points;
            else if(j == 6) conePoints = c7Points;

            for(int i=0; i < cTimes.size(); i++)
            {
                cVal = conePoints.get(i).getY();
                Log.i(TAG, "cVal (" + i + ", " + j + "): " + cVal);
                if(cVal != 0)
                {
                    // For cone distance averages
                    coneAvgs[j] += cVal;
                    coneNums[j]++;

                    Log.i(TAG, "CONE AVG " + j + ": " + coneAvgs);
                    Log.i(TAG, "CONE NUMS " + j + ": " + coneNums);

                    // Find value of left hand yaw at that time
                    if(lTimes.contains(cTimes.get(i)))
                    {
                        calcLeftHand = true;
                        leftYawAvgs[j] += lYawPoints.get(lTimes.indexOf(cTimes.get(i))).getY();
                        leftYawNums[j]++;

                        leftPitchAvgs[j] += lYawPoints.get(lTimes.indexOf(cTimes.get(i))).getY();
                        leftPitchNums[j]++;

                        leftRollAvgs[j] += lYawPoints.get(lTimes.indexOf(cTimes.get(i))).getY();
                        leftRollNums[j]++;

                    }

                    // Find value of right hand yaw at that time
                    if(rTimes.contains(cTimes.get(i)))
                    {
                        calcRightHand = true;
                        rightYawAvgs[j] += rYawPoints.get(rTimes.indexOf(cTimes.get(i))).getY();
                        rightYawNums[j]++;

                        rightPitchAvgs[j] += rYawPoints.get(rTimes.indexOf(cTimes.get(i))).getY();
                        rightPitchNums[j]++;

                        rightRollAvgs[j] += rYawPoints.get(rTimes.indexOf(cTimes.get(i))).getY();
                        rightRollNums[j]++;
                    }

                    if (stickTimes.contains(cTimes.get(i)))
                    {
                        calcStick = true;
                    }
                }
            }

            if(coneNums[j] != 0)
                coneAvgs[j] = coneAvgs[j]/(double)coneNums[j];

            // Log.i(TAG, "CONE AVG " + j + ": " + coneAvgs[j]);

            if(leftYawNums[j] != 0)
                leftYawAvgs[j] /= (leftYawAvgs[j]/(double)leftYawNums[j]);

            if(leftPitchNums[j] != 0)
                leftPitchAvgs[j] /= (leftPitchAvgs[j]/(double)leftPitchNums[j]);

            if(leftRollNums[j] != 0)
                leftRollAvgs[j] /= (leftRollAvgs[j]/(double)leftRollNums[j]);

            if(rightYawNums[j] != 0)
                rightYawAvgs[j] /= (rightYawAvgs[j]/(double)rightYawNums[j]);

            if(rightPitchNums[j] != 0)
                rightPitchAvgs[j] /= (rightPitchAvgs[j]/(double)rightPitchNums[j]);

            if(rightRollNums[j] != 0)
                rightRollAvgs[j] /= (rightRollAvgs[j]/(double)rightRollNums[j]);


            coneError[j] = Math.abs(((coneAvgs[j] - coneExpAvgs[j])/coneExpAvgs[j])*100);
            Log.i(TAG, "CONE ERROR " + j + ": " + coneError[j]);
            coneScore[j] = 5 + 0.05 * (coneError[j] - 100);
            Log.i(TAG, "CONE SCORE " + j + ": " + coneScore[j]);

            if(calcLeftHand)
            {
                leftYawError[j] = Math.abs(((leftYawAvgs[j] - leftYawExpAvgs[j])/leftYawExpAvgs[j])*100);
                leftYawScore[j] = 5 + 0.05 * (leftYawError[j] - 100);
                leftPitchError[j] = Math.abs(((leftPitchAvgs[j] - leftPitchExpAvgs[j])/leftPitchExpAvgs[j])*100);
                leftPitchScore[j] = 5 + 0.05 * (leftPitchError[j] - 100);
                leftRollError[j] = Math.abs(((leftRollAvgs[j] - leftRollExpAvgs[j])/leftRollExpAvgs[j])*100);
                leftRollScore[j] = 5 + 0.05 * (leftRollError[j] - 100);
            }

            if(calcRightHand)
            {
                rightYawError[j] = Math.abs(((rightYawAvgs[j] - rightYawExpAvgs[j])/rightYawExpAvgs[j])*100);
                rightYawScore[j] = 5 + 0.05 * (rightYawError[j] - 100);
                rightPitchError[j] = Math.abs(((rightPitchAvgs[j] - rightPitchExpAvgs[j])/rightPitchExpAvgs[j])*100);
                rightPitchScore[j] = 5 + 0.05 * (rightPitchError[j] - 100);
                rightRollError[j] = Math.abs(((rightRollAvgs[j] - rightRollExpAvgs[j])/rightRollExpAvgs[j])*100);
                rightRollScore[j] = 5 + 0.05 * (rightRollError[j] - 100);
            }

        }

        if(calcStick)
        {
            double stickAvg = 0.0;
            for(int k=0; k<stickPoints.size(); k++)
                stickAvg += stickPoints.get(k).getY();

            if(!stickPoints.isEmpty())
                stickAvg /= (double)stickPoints.size();

            stickError = Math.abs(((stickAvg - stickExpAvg)/stickExpAvg)*100);
            stickScore = 5 + 0.05 * (stickError - 100);
        }


    }

    public void giveRecommendations()
    {
        // Hand recommendations
        double coneAvgScore = 0.0;
        double leftYawAvgScore = 0.0;
        double leftPitchAvgScore = 0.0;
        double leftRollAvgScore = 0.0;
        double rightYawAvgScore = 0.0;
        double rightPitchAvgScore = 0.0;
        double rightRollAvgScore = 0.0;

        for(int i=0; i<7; i++)
        {
            coneAvgScore += coneScore[i];

            if(calcLeftHand)
            {
                leftYawAvgScore += leftYawScore[i];
                leftPitchAvgScore += leftPitchScore[i];
                leftRollAvgScore += leftRollScore[i];
            }

            if(calcRightHand)
            {
                rightYawAvgScore += rightYawScore[i];
                rightPitchAvgScore += rightPitchScore[i];
                rightRollAvgScore += rightRollScore[i];
            }
        }

        coneAvgScore /= 7.0;
        Log.i(TAG, "CONE AVG SCORE:" + coneAvgScore);


        double leftHandScore = 0.0, rightHandScore = 0.0;
        if(calcLeftHand)
        {
            leftYawAvgScore /= 7.0;
            leftPitchAvgScore /= 7.0;
            leftRollAvgScore /= 7.0;
            leftHandScore = (leftYawAvgScore + leftPitchAvgScore + leftRollAvgScore) / (double)3;
        }

        if(calcRightHand)
        {
            rightYawAvgScore /= 7.0;
            rightPitchAvgScore /= 7.0;
            rightRollAvgScore /= 7.0;
            rightHandScore = (rightYawAvgScore + rightPitchAvgScore + rightRollAvgScore) / (double)3;
        }

        double overallAvgScore = coneAvgScore;
        ArrayList<Double> scores = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        scores.add(coneAvgScore);
        names.add("CONES");
        if(calcLeftHand)
        {
            overallAvgScore += leftHandScore;
            scores.add(leftHandScore);
            names.add("LEFT");
        }
        if(calcRightHand)
        {
            overallAvgScore += rightHandScore;
            scores.add(rightHandScore);
            names.add("RIGHT");
        }
        if(calcStick)
        {
            overallAvgScore += stickScore;
            scores.add(stickScore);
            names.add("STICK");
        }

        overallAvgScore /= scores.size();

        String minScore = findMinScore(names, scores);

        String bottomLine = "For next time: ";
        if(minScore.equals("CONES")) // Cones were lowest score
        {
            int minConeScoreIndex = findMinScore(coneScore);
            bottomLine += "Try to keep the ball closer to the cones, specifically cone " + (minConeScoreIndex + 1);
        }
        else if(minScore.equals("LEFT")) // Left hand was lowest score
        {
            double[] leftScores = {leftYawAvgScore, leftPitchAvgScore, leftRollAvgScore};
            int minHandScoreIndex = findMinScore(leftScores);
            switch(minHandScoreIndex)
            {
                case 0: // yaw
                    bottomLine += "Try to keep your top hand away from your body while performing the drill.";
                    break;
                case 1: // pitch
                    bottomLine += "Try to use your top hand to control the stick by rotating your left wrist more while performing the drill.";
                    break;
                case 2: // roll
                    bottomLine += "Try to use your top hand to control the stick by rotating your left wrist more while performing the drill.";
                    break;
            }
        }
        else if(minScore.equals("RIGHT")) // Right hand was lowest score
        {
            double[] rightScores = {rightYawAvgScore, rightPitchAvgScore, rightRollAvgScore};
            int minHandScoreIndex = findMinScore(rightScores);
            switch(minHandScoreIndex)
            {
                case 0: // yaw
                    bottomLine += "Try to keep your bottom hand away from your body while performing the drill.";
                    break;
                case 1: // pitch
                    bottomLine += "Try to use your bottom hand to control the stick by rotating your left wrist more while performing the drill.";
                    break;
                case 2: // roll
                    bottomLine += "Try to use your bottom hand to control the stick by rotating your left wrist more while performing the drill.";
                    break;
            }
        }
        else if(minScore.equals("STICK")) // Stick was lowest score
        {
            bottomLine += "Try to keep your bottom hand further down on the stick to create better control over your stickhandling motions while performing the drill.";
        }

        String overallAssessment = "";
        if(overallAvgScore <= 1) overallAssessment = "Your stickhandling needs some improvement!";
        else if(overallAvgScore <= 2) overallAssessment = "Not bad!";
        else if(overallAvgScore <= 3) overallAssessment = "Good job!";
        else if(overallAvgScore <= 4) overallAssessment = "Awesome job!";
        else if(overallAvgScore < 5) overallAssessment = "Nearly perfect!";
        else overallAssessment = "Perfect!!";

        String recommendation = String.format("OVERALL SCORE: %.2f\t\t\t %s\n\n" +
                                "Breakdown of Score::\n" +
                                "\t\t\tScore for distance from cones:\t%.2f\n", overallAvgScore, overallAssessment, coneAvgScore);

        if(calcRightHand)
            recommendation += String.format("\t\t\tScore for right hand rotation:\t%.2f\n", rightHandScore);
        else
            recommendation += "\t\t\tScore for right hand rotation:\tN/A\n";

        if(calcLeftHand)
            recommendation += String.format("\t\t\tScore for left hand rotation:\t%.2f\n", leftHandScore);
        else
            recommendation += "\t\t\tScore for left hand rotation:\tN/A\n";

        if(calcStick)
            recommendation += "\t\t\tScore for bottom hand location:\t" + stickScore + "\n\n";
        else
            recommendation += "\t\t\tScore for bottom hand location:\tN/A\n\n";

        recommendation += bottomLine;

        recommendationText.setText(recommendation);
    }

    public String findMinScore(ArrayList<String> names, ArrayList<Double> scores)
    {
        double min = 6;
        int index = 0;
        for(int i=0; i<scores.size(); i++)
        {
            if(scores.get(i) < min)
            {
                min = scores.get(i);
                index = i;
            }
        }

        return names.get(index);
    }

    public int findMinScore(double[] scores)
    {
        double min = 6;
        int index = 0;
        for(int i=0; i<scores.length; i++)
        {
            if(scores[i] < min)
            {
                min = scores[i];
                index = i;
            }
        }

        return index;
    }

}
