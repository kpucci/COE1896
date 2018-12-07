package com.coe1896.puckperfectapp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
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

import com.coe1896.puckperfectapp.DataAnalysis.PuckPerfectData;
import com.coe1896.puckperfectapp.PuckPerfectDevice.DEVICE_TYPE;

public class RecordActivity extends AppCompatActivity {


    private static final String TAG = "RecordActivity";
    private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID

    // Bluetooth stuff
    BluetoothAdapter BTAdapter;
    public PuckPerfectDevice[] devices = new PuckPerfectDevice[4];

    private BluetoothMonitor btMonitor;

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

    boolean isTimerRunning = false;

    Calendar cal;
    SimpleDateFormat fmt;

    int practiceMin = 0;
    int practiceSec = 0;

    // Cone map stuff
    private ConeMap coneMap;
    private int numError1 = 0;
    private int numError2 = 0;

    private PopupWindow mPopupWindow;
    private Context mContext;
    private ConstraintLayout mConstraintLayout;

    Switch coneEnable;
    Switch stickEnable;
    Switch rGloveEnable;
    Switch lGloveEnable;

    View coneInd;
    View stickInd;
    View rGloveInd;
    View lGloveInd;


    View[] indicators = new View[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        mContext = getApplicationContext();

        Log.i(TAG, "----- ON CREATE METHOD INVOKED -----");

        // Create the SurfaceViewThread object.
        // coneMap = new ConeMap(getApplicationContext());

        // Get frame layout
        // FrameLayout coneLayout = (FrameLayout) findViewById(R.id.coneGridConstraintLayout);
        // coneLayout.addView(coneMap);

        mConstraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);

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

        coneInd = (View) findViewById(R.id.coneIndicator);
        stickInd = (View) findViewById(R.id.stickIndicator);
        rGloveInd = (View) findViewById(R.id.rGloveIndicator);
        lGloveInd = (View) findViewById(R.id.lGloveIndicator);

        indicators[DEVICE_TYPE.CONES.getIndex()] = coneInd;
        indicators[DEVICE_TYPE.STICK.getIndex()] = stickInd;
        indicators[DEVICE_TYPE.R_GLOVE.getIndex()] = rGloveInd;
        indicators[DEVICE_TYPE.L_GLOVE.getIndex()] = lGloveInd;

        coneEnable = (Switch) findViewById(R.id.coneEnable);
        coneEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) //Line A
            {
                int index = DEVICE_TYPE.CONES.getIndex();
                if(devices[index] != null)
                {
                    devices[index].setEnabled(isChecked);
                    if(isChecked)
                        coneInd.setBackgroundResource(R.drawable.circle_red);
                    else
                        coneInd.setBackgroundResource(R.drawable.circle_gray);
                }
                else
                {
                    coneInd.setBackgroundResource(R.drawable.circle_gray);
                }

            }
        });

        stickEnable = (Switch) findViewById(R.id.stickEnable);
        stickEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) //Line A
            {
                int index = DEVICE_TYPE.STICK.getIndex();
                if(devices[index] != null)
                {
                    devices[index].setEnabled(isChecked);
                    if(isChecked)
                        stickInd.setBackgroundResource(R.drawable.circle_red);
                    else
                        stickInd.setBackgroundResource(R.drawable.circle_gray);
                }
                else
                {
                    stickInd.setBackgroundResource(R.drawable.circle_gray);
                }

            }
        });

        rGloveEnable = (Switch) findViewById(R.id.rGloveEnable);
        rGloveEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) //Line A
            {
                int index = DEVICE_TYPE.R_GLOVE.getIndex();
                if(devices[index] != null)
                {
                    devices[index].setEnabled(isChecked);
                    if(isChecked)
                        rGloveInd.setBackgroundResource(R.drawable.circle_red);
                    else
                        rGloveInd.setBackgroundResource(R.drawable.circle_gray);
                }
                else
                {
                    rGloveInd.setBackgroundResource(R.drawable.circle_gray);
                }

            }
        });

        lGloveEnable = (Switch) findViewById(R.id.lGloveEnable);
        lGloveEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) //Line A
            {
                int index = DEVICE_TYPE.L_GLOVE.getIndex();
                if(devices[index] != null)
                {
                    devices[index].setEnabled(isChecked);
                    if(isChecked)
                        lGloveInd.setBackgroundResource(R.drawable.circle_red);
                    else
                        lGloveInd.setBackgroundResource(R.drawable.circle_gray);
                }
                else
                {
                    lGloveEnable.setBackgroundResource(R.drawable.circle_gray);
                }
            }
        });


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

        if(this.btMonitor == null || !this.btMonitor.isRunning())
            btMonitor = new BluetoothMonitor();

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
        {
            if(device != null && device.getSocket() != null)
            {
                DisconnectDevice disconnectTask = new DisconnectDevice();
                disconnectTask.device = device;
                disconnectTask.execute();
            }
        }

        // Stop bluetooth monitor
        if(this.btMonitor != null)
        {
            this.btMonitor.stop();
        }

        // Stop timer
        if(isTimerRunning)
            endTimer();

        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        if(this.btMonitor != null)
        {
            this.btMonitor.stop();
        }

        if(countDownTimer != null)
            countDownTimer.cancel();

        super.onDestroy();
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
     * Stop acquiring data from modules
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
        practiceSec = 0;

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

        btMonitor.stop();

        // Clear debug text
        clearDebugText();

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
                endTimer();
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
    public void endTimerHandler(View view)
    {
        endTimer();
    }

    public void endTimer()
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

        endDrill();

        // Restart bluetooth monitor;
        if(this.btMonitor != null)
            btMonitor.stop();

        btMonitor = new BluetoothMonitor();

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.save_practice_popup,null);

        mPopupWindow = new PopupWindow(
                customView,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        // Get a reference for the custom view close button
        Button cancelButton = (Button) customView.findViewById(R.id.cancelButton);

        // Set a click listener for the popup window close button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                Log.i(TAG, "Clicked cancel button");
                mPopupWindow.dismiss();
            }
        });

        // Get a reference for the custom view close button
        Button discardButton = (Button) customView.findViewById(R.id.discardButton);

        // Set a click listener for the popup window close button
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                Log.i(TAG, "Clicked discard button");
                mPopupWindow.dismiss();
            }
        });

        // Get a reference for the custom view close button
        Button saveButton = (Button) customView.findViewById(R.id.saveButton);

        // Set a click listener for the popup window close button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                // TODO: Save data to database
                Log.i(TAG, "Clicked save button");
                mPopupWindow.dismiss();

                if(btMonitor != null)
                {
                    btMonitor.stop();
                    btMonitor = null;
                }

                // Go to results activity
                Intent intent = new Intent(getApplicationContext(), ResultsActivity.class);
                startActivity(intent);
            }
        });

        mPopupWindow.showAtLocation(mConstraintLayout, Gravity.CENTER,0,0);
    }

    public void endDrill()
    {

        for(PuckPerfectDevice device : devices)
        {
            if(device != null && device.isConnected())
                device.exportData(this);
        }
    }

    public void clearDebugText()
    {
        if(leftGloveDebug != null) leftGloveDebug.setText("");
        if(rightGloveDebug != null) rightGloveDebug.setText("");
        if(coneDebug != null) coneDebug.setText("");
        if(stickDebug != null) stickDebug.setText("");
    }

    public void stopMonitor(View view)
    {
        if(btMonitor != null)
        {
            btMonitor.stop();
            btMonitor = null;
        }
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
                    {
                        devices[index] = new PuckPerfectDevice(device);
                        devices[index].setContext(getApplicationContext());
                    }

                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Log.i(TAG, "----- SEARCH DEVICES: ON POST EXECUTE METHOD INVOKED -----");

            if(btMonitor != null)
                btMonitor.stop();


            btMonitor = new BluetoothMonitor();
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

    private class BluetoothMonitor implements Runnable {

        private volatile boolean bStop = false;
        private Thread t;
        private TextView debugTextView = null;
        private ScrollView debugScrollView = null;
        private PuckPerfectDevice ppDevice;

        public BluetoothMonitor() {
            Log.i(TAG, "----- BLUETOOTH MONITOR: CONSTRUCTOR INVOKED -----");
            t = new Thread(this, "Input Thread");
            t.start();
        }

        public boolean isRunning() {
            return t.isAlive();
        }

        @Override
        public void run() {
            try
            {
                while(!bStop)
                {
                    for(PuckPerfectDevice device : devices)
                    {
                        ppDevice = device;
                        // Check if device is connected
                        if(device != null && !device.isConnected() && device.isEnabled() && !bStop)
                        {
                            Log.i(TAG, device.getName() + " is not connected. Connecting now");
                            // If not connected, try to connect it
                            device.connect();
                            if(device.isConnected())
                            {
                                Log.i(TAG, device.getName() + " is connected.");
                                indicators[device.getType().getIndex()].setBackgroundResource(R.drawable.circle_green);
                            }

                        }
                        else if(device != null && device.isConnected())
                        {
                            Log.i(TAG, device.getName() + " is connected.");
                            indicators[device.getType().getIndex()].setBackgroundResource(R.drawable.circle_green);
                        }
                        else if(device != null && !device.isEnabled())
                        {
                            DisconnectDevice disconnect = new DisconnectDevice();
                            disconnect.device = device;
                            disconnect.execute();
                        }
                    }

                    Thread.sleep(5000); // Check every 15s
                }
            }
            catch(InterruptedException e)
            {
                indicators[ppDevice.getType().getIndex()].setBackgroundResource(R.drawable.circle_red);
                e.printStackTrace();
            }
        }

        public void stop() {
            Log.i(TAG, "----- BLUETOOTH MONITOR: STOP METHOD INVOKED -----");
            bStop = true;
        }

    }
}
