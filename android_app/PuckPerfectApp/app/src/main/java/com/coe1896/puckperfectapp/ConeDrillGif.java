package com.coe1896.puckperfectapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/**
 * Class to animate puck moving in figure 8 motion
 */
public class ConeDrillGif extends SurfaceView implements SurfaceHolder.Callback, Runnable{

    private SurfaceHolder surfaceHolder = null;

    private Paint paint = null;

    private Thread thread = null;

    // Record whether the child thread is running or not.
    private boolean threadRunning = false;

    private Canvas canvas = null;

    // Puck path coordinates
    private static int[] x_coords = {400, 435, 500, 600, 700, 730, 715, 605, 495, 400, 300, 165, 80, 80, 165, 285, 365};
    private static int[] y_coords = {300, 250, 180, 145, 190, 260, 375, 455, 405, 300, 180, 160, 255, 375, 455, 425, 360};

    private int index = 0;

    private static String TAG = "CONE_DRILL_GIF";

    public ConeDrillGif(Context context) {
        super(context);

        setFocusable(true);

        // Get SurfaceHolder object.
        surfaceHolder = this.getHolder();
        // Add current object as the callback listener.
        surfaceHolder.addCallback(this);

        // Create the paint object which will draw the text.
        paint = new Paint();
        paint.setColor(Color.BLACK);


        // Set the SurfaceView object at the top of View object.
        setZOrderOnTop(true);

        // Set background to transparent
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
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

            drawPuck();

            long endTime = System.currentTimeMillis();

            long deltaTime = endTime - startTime;

            // Gets around screen refreshing
            if(deltaTime < 750)
            {
                try {
                    Thread.sleep(750 - deltaTime);
                }catch (InterruptedException ex)
                {
                    // TODO: Error handling
                }

            }
        }
    }

    /**
     * Draw puck on surface view
     */
    private void drawPuck()
    {
        canvas = surfaceHolder.lockCanvas();

        // Paint a transparent background -- this removes any old pucks that were drawn
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // Draw puck
        canvas.drawCircle(x_coords[index], y_coords[index], 30, paint);

        // Increment index to move along path
        index++;
        if(index == 17)
            index = 0;

        // Send message to main UI thread to update the drawing to the main view special area
        surfaceHolder.unlockCanvasAndPost(canvas);
    }
}





