package com.coe1896.puckperfectapp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import com.coe1896.puckperfectapp.PuckPerfectDevice.DEVICE_TYPE;

public class RecordActivity extends AppCompatActivity {


    private static final String TAG = "RecordActivity";
    private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID

    // Bluetooth stuff
    BluetoothAdapter BTAdapter;
    public PuckPerfectDevice[] devices = new PuckPerfectDevice[4];

    private static final int BT_ENABLE_REQUEST = 10;

    private boolean isUserInitiatedDisconnect = false;

    static final int CONNECT_REQUEST_1 = 1;  // The request code
    static final int CONNECT_REQUEST_2 = 2;  // The request code
    static final int CONNECT_REQUEST_3 = 3;  // The request code
    static final int CONNECT_REQUEST_4 = 4;  // The request code

    private ProgressDialog progressDialog;

    // Timer stuff
    CountDownTimer countDownTimer;
    TextView secTime;
    TextView minTime;
    TextView leftGloveDebug;
    TextView rightGloveDebug;
    TextView coneDebug;
    TextView stickDebug;
    ScrollView leftGloveScroll;
    ScrollView rightGloveScroll;
    ScrollView coneScroll;
    ScrollView stickScroll;

    Button startButton;
    Button endButton;
    Button pauseButton;

    Calendar cal;
    SimpleDateFormat fmt;

    int practiceMin = 0;
    int practiceSec = 0;

    // Cone map stuff
    private ConeMap coneMap;
    private int numError1 = 0;
    private int numError2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Log.i(TAG, "----- ON CREATE METHOD INVOKED -----");

        // Create the SurfaceViewThread object.
        coneMap = new ConeMap(getApplicationContext());

        // Get frame layout
        FrameLayout coneLayout = (FrameLayout) findViewById(R.id.coneGridConstraintLayout);
        coneLayout.addView(coneMap);

        secTime = (TextView) findViewById(R.id.secText);
        minTime = (TextView) findViewById(R.id.minText);
        startButton = (Button) findViewById(R.id.startButton);
        pauseButton = (Button) findViewById(R.id.pauseButton);
        endButton = (Button) findViewById(R.id.endButton);

        pauseButton.setEnabled(false);
        endButton.setEnabled(false);

        TextView colonText = (TextView) findViewById(R.id.colonText);
        colonText.setEnabled(false);

        leftGloveDebug = (TextView) findViewById(R.id.leftGloveDebugText);
        leftGloveScroll = (ScrollView) findViewById(R.id.leftGloveDebugScroll);
        leftGloveDebug.setMovementMethod(new ScrollingMovementMethod());

        rightGloveDebug = (TextView) findViewById(R.id.rightGloveDebugText);
        rightGloveScroll = (ScrollView) findViewById(R.id.rightGloveDebugScroll);
        rightGloveDebug.setMovementMethod(new ScrollingMovementMethod());

        coneDebug = (TextView) findViewById(R.id.coneDebugText);
        coneScroll = (ScrollView) findViewById(R.id.coneDebugScroll);
        coneDebug.setMovementMethod(new ScrollingMovementMethod());

        stickDebug = (TextView) findViewById(R.id.stickDebugText);
        stickScroll = (ScrollView) findViewById(R.id.stickDebugScroll);
        stickDebug.setMovementMethod(new ScrollingMovementMethod());

        fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        // Make sure bluetooth is connected
        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        if (BTAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth not found", Toast.LENGTH_SHORT).show();
        } else if (!BTAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, BT_ENABLE_REQUEST);
        } else {
            new SearchDevices().execute();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Log.i(TAG, "----- ON START METHOD INVOKED -----");

        boolean doConnect = false;

        for(PuckPerfectDevice device : devices)
        {
            if(device != null)
                Log.i(TAG, device.toString());
            if(device != null && !device.isConnected())
                doConnect = true;
        }

        if(doConnect)
            new ConnectDevices().execute();

    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(progressDialog != null )
            progressDialog.dismiss();
    }

    @Override
    protected void onStop()
    {
        Log.i(TAG, "----- ON STOP METHOD INVOKED -----");
        for(PuckPerfectDevice device : devices)
            if(device != null && device.getSocket() != null && device.isConnected())
            {
                DisconnectDevice disconnectTask = new DisconnectDevice();
                disconnectTask.device = device;
                disconnectTask.execute();
            }

        super.onStop();
    }

    /**
     * Send start character to bluetooth modules
     */
    private void startModules()
    {
        Log.i(TAG, "----- START MODULES METHOD INVOKED -----");
        for(PuckPerfectDevice device : devices)
        {
            if(device != null)
                device.startAcquire();
        }
    }

    /**
     * Send start character to bluetooth modules
     */
    private void startModulesDebug()
    {
        Log.i(TAG, "----- START MODULES METHOD INVOKED -----");
        for(PuckPerfectDevice device : devices)
        {
            if(device != null)
            {
                if(device.isConnected())
                {
                    // DEBUG MODE
                    switch(device.getType())
                    {
                        case CONES:
                            device.startAcquire(coneDebug, coneScroll);
                            break;
                        case STICK:
                            device.startAcquire(stickDebug, stickScroll);
                            break;
                        case L_GLOVE:
                            device.startAcquire(leftGloveDebug, leftGloveScroll);
                            break;
                        case R_GLOVE:
                            device.startAcquire(rightGloveDebug, rightGloveScroll);
                            break;
                    }
                }
            }
        }
    }


    /**
     * Send end character to bluetooth modules
     */
    private void stopModules()
    {
        Log.i(TAG, "----- STOP MODULES METHOD INVOKED -----");
        for(PuckPerfectDevice device : devices)
        {
            if(device != null)
                device.stopAcquire();
        }
    }

    // --------------------------------------------------------------------------
    // ---------- TIMER ----------

    /**
     * Start timer button handler
     * @param view
     */
    public void startTimerHandler(View view)
    {
        practiceMin = 0;

        if(secTime.getText().length() != 0)
        {
            int tempSec = Integer.parseInt(secTime.getText().toString());
            if(tempSec > 59)
            {
                practiceSec = tempSec % 60;
                practiceMin = tempSec / 60;
            }
            else
                practiceSec = tempSec;
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Please enter a time for seconds", Toast.LENGTH_LONG).show();
            return;
        }

        if(minTime.getText().length() != 0)
        {
            int tempMin = Integer.parseInt(minTime.getText().toString());
            practiceMin = practiceMin + tempMin;
        }

        startTimer(practiceSec, practiceMin);

        secTime.setEnabled(false);
        minTime.setEnabled(false);
    }

    /**
     * Start the practice timer
     * @param seconds
     * @param minutes
     */
    private void startTimer(int seconds, int minutes)
    {
        Log.i(TAG, "----- START TIMER METHOD INVOKED -----");
        int timeInSec = (minutes * 60) + seconds;
        numError1 = 0;
        numError2 = 0;

        if(timeInSec > 600)
        {
            Toast.makeText(getApplicationContext(), "Must limit time to 10 minutes!", Toast.LENGTH_LONG).show();
            return;
        }

        Log.i(TAG, "----- START MODULES -----");
        startModulesDebug();

        countDownTimer = new CountDownTimer(timeInSec * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                secTime.setText(String.format("%02d",seconds));
                minTime.setText(String.format("%02d",minutes));

            }
            public void onFinish() {
                // Tell bluetooth modules to stop acquiring
                stopModules();

                secTime.setText(String.format("%02d",practiceSec));
                minTime.setText(String.format("%02d",practiceMin));

                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startTimerHandler(view);
                    }
                });

                startButton.setEnabled(true);
                pauseButton.setEnabled(false);
                endButton.setEnabled(false);

                secTime.setEnabled(true);
                minTime.setEnabled(true);

                calculateScore();
            }
        };
        countDownTimer.start();

        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
        endButton.setEnabled(true);
    }

    /**
     * Restart timer button handler
     * @param view
     */
    public void restartTimerHandler(View view)
    {
        int seconds = Integer.parseInt(secTime.getText().toString());
        int minutes = Integer.parseInt(minTime.getText().toString());

        startTimer(seconds, minutes);
    }

    /**
     * Pause timer button handler
     * @param view
     */
    public void pauseTimer(View view)
    {
        // Tell bluetooth modules to stop acquiring
        stopModules();

        countDownTimer.cancel();

        pauseButton.setEnabled(false);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartTimerHandler(view);
            }
        });

        startButton.setEnabled(true);
    }

    /**
     * End timer button handler
     * @param view
     */
    public void endTimer(View view)
    {
        stopModules();

        countDownTimer.cancel();
        secTime.setText(String.format("%02d",practiceSec));
        minTime.setText(String.format("%02d",practiceMin));

        pauseButton.setEnabled(false);
        endButton.setEnabled(false);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimerHandler(view);
            }
        });
        startButton.setEnabled(true);

        secTime.setEnabled(true);
        minTime.setEnabled(true);

        calculateScore();
    }


    // --------------------------------------------------------------------------
    // ---------- DATA ANALYSIS ----------

    public void calculateScore()
    {
        if(numError2 > numError1)
            Toast.makeText(getApplicationContext(),"Too far from cone 2",Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getApplicationContext(),"Too far from cone 1",Toast.LENGTH_LONG).show();
    }

    // --------------------------------------------------------------------------
    // ---------- BLUETOOTH ----------

    /**
     * Search paired devices for cones, stick, left glove, and right glove modules
     */
    private class SearchDevices extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Log.i(TAG, "----- SEARCH DEVICES: DO IN BACKGROUND METHOD INVOKED -----");
            Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();

            String name;
            for (BluetoothDevice device : pairedDevices) {
                name = device.getName().trim();
                Log.i(TAG, "~~~~~ DEVICE: " + name + " ~~~~~");
                if(DEVICE_TYPE.isPuckPerfectDevice(name))
                {
                    Log.i(TAG, name);
                    int index = DEVICE_TYPE.getIndexFromName(name);
                    if(index >= 0)
                        devices[index] = new PuckPerfectDevice(device);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Log.i(TAG, "----- SEARCH DEVICES: ON POST EXECUTE METHOD INVOKED -----");

            // if(deviceTable.contains("CONES"))
            //     devices[DEVICE_TYPE.CONES.getIndex()] = new PuckPerfectDevice(deviceTable.get("CONES"));
            // else
            //     Toast.makeText(getApplicationContext(), "WARNING: Could not find paired module for cones. Please pair the device.", Toast.LENGTH_SHORT).show();

            // if(deviceTable.contains("STICK"))
            // {
            //     devices[DEVICE_TYPE.STICK.getIndex()] = new PuckPerfectDevice(deviceTable.get("STICK"));
            //     Log.i(TAG, devices[DEVICE_TYPE.STICK.getIndex()].toString());
            // }
            // else
            // {
            //     Log.i(TAG, "~~~~~ COULD NOT FIND STICK ~~~~~");
            //     Toast.makeText(getApplicationContext(), "WARNING: Could not find paired module for stick. Please pair the device.", Toast.LENGTH_SHORT).show();
            // }
            //
            // if(deviceTable.contains("L_GLOVE"))
            //     devices[DEVICE_TYPE.L_GLOVE.getIndex()] = new PuckPerfectDevice(deviceTable.get("L_GLOVE"));
            // else
            //     Toast.makeText(getApplicationContext(), "WARNING: Could not find paired module for left glove. Please pair the device.", Toast.LENGTH_SHORT).show();
            //
            // if(deviceTable.contains("R_GLOVE"))
            //     devices[DEVICE_TYPE.R_GLOVE.getIndex()] = new PuckPerfectDevice(deviceTable.get("R_GLOVE"));
            // else
            //     Toast.makeText(getApplicationContext(), "WARNING: Could not find paired module for right glove. Please pair the device.", Toast.LENGTH_SHORT).show();

            new ConnectDevices().execute();
        }
    }

    /**
     * Connect the devices
     */
    private class ConnectDevices extends AsyncTask<Void, Integer, Void>
    {

        private boolean connectSuccessful = true;

        @Override
        protected void onPreExecute()
        {
            Log.i(TAG, "----- CONNECT DEVICES: ON PRE EXECUTE METHOD INVOKED -----");
            if(devices[0] != null || devices[1] != null || devices[2] != null || devices[3] != null)
               progressDialog = ProgressDialog.show(RecordActivity.this, "Connecting", "Connecting to devices");// http://stackoverflow.com/a/11130220/1287554
            else
                progressDialog = ProgressDialog.show(RecordActivity.this, "Error", "No paired devices. Please pair the PuckPerfect devices.");
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            Log.i(TAG, "----- CONNECT DEVICES: DO IN BACKGROUND METHOD INVOKED -----");
            for(PuckPerfectDevice device : devices)
            {
                if(device != null)
                {
                    device.connect();
                    if(!device.isConnected())
                        connectSuccessful = false;
                }
                else
                    connectSuccessful = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            progressDialog.dismiss();
        }
    }

    private class DisconnectDevice extends AsyncTask<Void, Void, Void>
    {
        PuckPerfectDevice device;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            Log.i(TAG, "----- DISCONNECT DEVICES: DO IN BACKGROUND METHOD INVOKED -----");

            device.disconnect();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Log.i(TAG, "----- CONNECT DEVICES: ON POST EXECUTE METHOD INVOKED -----");
            device.setConnectionState(false);
        }
    }

    // ---------- Thread for checking bluetooth connections ----------

    // private class BluetoothMonitor implements Runnable {
    //
    //     private boolean bStop = false;
    //     private Thread t;
    //     private TextView debugTextView = null;
    //     private ScrollView debugScrollView = null;
    //
    //     public BluetoothMonitor() {
    //         Log.i(TAG, "----- INPUT READER: CONSTRUCTOR INVOKED -----");
    //         t = new Thread(this, "Input Thread");
    //         t.start();
    //     }
    //
    //     public BluetoothMonitor(TextView debugTextView, ScrollView debugScrollView) {
    //         Log.i(TAG, "----- INPUT READER: CONSTRUCTOR 2 INVOKED -----");
    //         this.debugTextView = debugTextView;
    //         this.debugScrollView = debugScrollView;
    //
    //         t = new Thread(this, "Input Thread");
    //         t.start();
    //     }
    //
    //     public boolean isRunning() {
    //         return t.isAlive();
    //     }
    //
    //     @Override
    //     public void run() {
    //         Log.i(TAG, "----- INPUT READER: RUNNING -----");
    //         InputStream inputStream;
    //
    //         try {
    //             inputStream = socket.getInputStream();
    //             while (!bStop) {
    //                 byte[] buffer = new byte[256];
    //                 if (inputStream != null && inputStream.available() > 0) {
    //                     inputStream.read(buffer);
    //                     int i = 0;
    //                     /*
    //                      * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
    //                      */
    //                     for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
    //                     }
    //                     final String strInput = new String(buffer, 0, i);
    //                     data.storeData(strInput);
    //
    //                     // FOR DEBUG PURPOSES ONLY
    //                     if(debugTextView != null)
    //                     {
    //                         final String dataDebug = data.printLastDataPoint();
    //
    //
    //                         debugTextView.post(new Runnable() {
    //                             @Override
    //                             public void run() {
    //                                 debugTextView.append(dataDebug);
    //
    //                                 int txtLength = debugTextView.getEditableText().length();
    //                                 if(txtLength > maxChars){
    //                                     debugTextView.getEditableText().delete(0, txtLength - maxChars);
    //                                 }
    //
    //                                 debugScrollView.post(new Runnable() { // Snippet from http://stackoverflow.com/a/4612082/1287554
    //                                     @Override
    //                                     public void run() { debugScrollView.fullScroll(View.FOCUS_DOWN);
    //                                     }
    //                                 });
    //                             }
    //                         });
    //
    //                     }
    //
    //                 }
    //                 Thread.sleep(500);
    //             }
    //         } catch (IOException e) {
    //             e.printStackTrace();
    //         } catch (InterruptedException e) {
    //             e.printStackTrace();
    //         }
    //
    //     }
    //
    //     public void stop() {
    //         Log.i(TAG, "----- INPUT READER: STOP METHOD INVOKED -----");
    //         bStop = true;
    //     }
    //
    // }
}
