package com.coe1896.puckperfectapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
//import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class ConeMap extends SurfaceView implements SurfaceHolder.Callback, Runnable{

    private SurfaceHolder surfaceHolder = null;

    private Paint paint = null;
    private Paint sensorPaint = null;
    private Paint currPaint = null;

    private Thread thread = null;

    // Record whether the child thread is running or not.
    private boolean threadRunning = false;

    private Canvas canvas = null;

    private ArrayList<Integer> x_coords = new ArrayList<>();
    private ArrayList<Integer> y_coords = new ArrayList<>();


    private static String LOG_TAG = "SURFACE_VIEW_THREAD";

    public ConeMap(Context context) {
        super(context);

        setFocusable(true);

        // Get SurfaceHolder object.
        surfaceHolder = this.getHolder();
        // Add current object as the callback listener.
        surfaceHolder.addCallback(this);

        // Create the paint object which will draw the text.
        paint = new Paint();
//        paint.setTextSize(100);
        paint.setColor(Color.GREEN);

        sensorPaint = new Paint();
        sensorPaint.setColor(Color.GRAY);

        currPaint = new Paint();
        currPaint.setColor(Color.RED);

        // Set the SurfaceView object at the top of View object.
        setZOrderOnTop(true);

        //setBackgroundColor(Color.RED);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        // Create the child thread when SurfaceView is created.
        thread = new Thread(this);
        // Start to run the child thread.
        thread.start();
        // Set thread running flag to true.
        threadRunning = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // Set thread running flag to false when Surface is destroyed.
        // Then the thread will jump out the while loop and complete.
        threadRunning = false;
    }

    @Override
    public void run() {
        while(threadRunning)
        {

            long startTime = System.currentTimeMillis();

            drawMap();

            long endTime = System.currentTimeMillis();

            long deltaTime = endTime - startTime;

            if(deltaTime < 100)
            {
                try {
                    Thread.sleep(100 - deltaTime);
                }catch (InterruptedException ex)
                {
//                    Log.e(LOG_TAG, ex.getMessage());
                }

            }
        }
    }

    private void drawMap()
    {
        // Only draw text on the specified rectangle area.
        canvas = surfaceHolder.lockCanvas();

        // Draw sensors
        Rect sensor1 = new Rect(200, 500, 400, 400);
        canvas.drawRect(sensor1, sensorPaint);

        Rect sensor2 = new Rect(600, 500, 800, 400);
        canvas.drawRect(sensor2, sensorPaint);

        int num_points = x_coords.size();

        // Draw points
        for(int i=0; i<num_points-1; i++) {
            canvas.drawCircle(x_coords.get(i), y_coords.get(i), 10, paint);
        }

        if(num_points != 0)
            canvas.drawCircle(x_coords.get(num_points-1), y_coords.get(num_points-1), 10, currPaint);

        // Send message to main UI thread to update the drawing to the main view special area.
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    public void addPoints(int x1, int y1, int x2, int y2)
    {
        x_coords.add(x1);
        y_coords.add(y1);

        x_coords.add(x2);
        y_coords.add(y2);
    }

    public void addPoint(int x1, int y1)
    {
        x_coords.add(x1);
        y_coords.add(y1);
    }
}




