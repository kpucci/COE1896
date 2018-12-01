package com.coe1896.puckperfectapp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.coe1896.puckperfectapp.DataAnalysis.ConeData;
import com.coe1896.puckperfectapp.DataAnalysis.GloveData;
import com.coe1896.puckperfectapp.DataAnalysis.PuckPerfectData;
import com.coe1896.puckperfectapp.DataAnalysis.StickData;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.UUID;

public class PuckPerfectDevice {

    public enum DEVICE_TYPE {
        CONES(0),
        STICK(1),
        R_GLOVE(2),
        L_GLOVE(3);

        private final int index;

        DEVICE_TYPE(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

        public static int getIndexFromName(String name) {
            int index = -1;

            try
            {
                index = valueOf(name).getIndex();
            }
            catch(IllegalArgumentException e)
            {
                e.printStackTrace();
            }

            return index;
        }

        public static boolean isPuckPerfectDevice(String name)
        {
            try
            {
                valueOf(name);
                return true;
            }
            catch(IllegalArgumentException e)
            {
                return false;
            }
        }
    }

    public String TAG = "PuckPerfectDevice";
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private UUID deviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID
    private InputReader reader;
    private DEVICE_TYPE type;
    private boolean connectionState;
    private PuckPerfectData data;

    private int maxChars = 50000;//Default


    public PuckPerfectDevice(BluetoothDevice device)
    {
        this.device = device;
        this.socket = null;
        this.reader = null;
        this.type = DEVICE_TYPE.valueOf(device.getName().trim());
        switch(this.type)
        {
            case CONES:
                this.data = new ConeData();
                break;
            case STICK:
                this.data = new StickData();
                break;
            case L_GLOVE:
            case R_GLOVE:
                this.data = new GloveData();
                break;
        }

        this.connectionState = false;
    }

    public BluetoothDevice getBluetoothDevice() {
        return this.device;
    }

    public BluetoothSocket getSocket() {
        return this.socket;
    }

    public InputReader getInputReader() {
        return this.reader;
    }

    public DEVICE_TYPE getType() {
        return this.type;
    }

    public String getName() {
        return this.device.getName();
    }

    public boolean isConnected() {
        return connectionState;
    }

    public void setSocket(BluetoothSocket socket) {
        this.socket = socket;
    }

    public void createInputReader(){
        this.reader = new InputReader();
    }

    public void createInputReader(TextView debugTextView, ScrollView debugScrollView)
    {
        this.reader = new InputReader(debugTextView, debugScrollView);
    }

    public void setConnectionState(boolean connectionState)
    {
        this.connectionState = connectionState;
    }

    public void disconnect()
    {
        Log.i(TAG, "----- DISCONNECT METHOD INVOKED -----");

        if(reader != null)
        {
            reader.stop();
            while (reader.isRunning());
            reader = null;
        }

        if(socket != null)
        {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void connect()
    {
        Log.i(TAG, "----- CONNECT METHOD INVOKED -----");
        try {
            if(socket == null || !connectionState) {
                socket = device.createInsecureRfcommSocketToServiceRecord(deviceUUID);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                socket.connect();
                connectionState = true;
            }
        } catch(IOException e) {
            // Unable to connect to device
            e.printStackTrace();
            connectionState = false;
        }
    }

    /**
     * Send start character to device
     */
    public void startAcquire() throws IOException
    {
        Log.i(TAG, "----- START ACQUIRE METHOD INVOKED -----");
        if(isConnected())
            socket.getOutputStream().write('s');
    }

    /**
     * Send end character to device
     */
    public void stopAcquire() throws IOException
    {
        Log.i(TAG, "----- STOP ACQUIRE METHOD INVOKED -----");
        if(isConnected())
            socket.getOutputStream().write('e');
    }


    @Override
    public String toString()
    {
        String output = "PuckPerfect Device: " + device.getName() + "\n" +
                        "   Connection Status: " + connectionState + "\n";

        return output;
    }

    // ----- Input Stream Reader Inner Class -----

    private class InputReader implements Runnable {

        private boolean bStop = false;
        private Thread t;
        private TextView debugTextView = null;
        private ScrollView debugScrollView = null;

        public InputReader() {
            Log.i(TAG, "----- INPUT READER: CONSTRUCTOR INVOKED -----");
            t = new Thread(this, "Input Thread");
            t.start();
        }

        public InputReader(TextView debugTextView, ScrollView debugScrollView) {
            Log.i(TAG, "----- INPUT READER: CONSTRUCTOR 2 INVOKED -----");
            this.debugTextView = debugTextView;
            this.debugScrollView = debugScrollView;

            t = new Thread(this, "Input Thread");
            t.start();
        }

        public boolean isRunning() {
            return t.isAlive();
        }

        @Override
        public void run() {
            Log.i(TAG, "----- INPUT READER: RUNNING -----");
            InputStream inputStream;

            try {
                inputStream = socket.getInputStream();
                while (!bStop) {
                    byte[] buffer = new byte[24];
                    if (inputStream != null && inputStream.available() > 0) {
                        inputStream.read(buffer);
                        int i = 0;
                        /*
                         * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
                         */
                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
                        }
                        final String strInput = new String(buffer, 0, i);
                        data.storeData(strInput);

                        // FOR DEBUG PURPOSES ONLY
                        if(debugTextView != null)
                        {
                            final String dataDebug = data.printLastDataPoint();


                            debugTextView.post(new Runnable() {
                                @Override
                                public void run() {
                                    debugTextView.append(dataDebug);

                                    int txtLength = debugTextView.getEditableText().length();
                                    if(txtLength > maxChars){
                                        debugTextView.getEditableText().delete(0, txtLength - maxChars);
                                    }

                                    debugScrollView.post(new Runnable() { // Snippet from http://stackoverflow.com/a/4612082/1287554
                                        @Override
                                        public void run() { debugScrollView.fullScroll(View.FOCUS_DOWN);
                                        }
                                    });
                                }
                            });

                        }

                    }
                    Thread.sleep(500);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        public void stop() {
            Log.i(TAG, "----- INPUT READER: STOP METHOD INVOKED -----");
            bStop = true;
        }

    }
}
