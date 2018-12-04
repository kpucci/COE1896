package com.coe1896.puckperfectapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
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
import java.util.UUID;

public class PuckPerfectDevice {

    public enum DEVICE_TYPE {
        CONES(0, 256),
        STICK(1, 4),
        R_GLOVE(2, 29),
        L_GLOVE(3, 29);

        private final int index;
        private final int buffSize;

        DEVICE_TYPE(int index, int buffSize) {
            this.index = index;
            this.buffSize = buffSize;
        }

        public int getIndex() {
            return this.index;
        }
        public int getBuffSize() {
            return this.buffSize;
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

        public static int getBuffSizeFromName(String name) {
            int size = -1;

            try
            {
                size = valueOf(name).getBuffSize();
            }
            catch(IllegalArgumentException e)
            {
                e.printStackTrace();
            }

            return size;
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
    private boolean acquire = false;
    private int buffSize;

    private int maxChars = 50000;//Default

    public Context context;


    public PuckPerfectDevice(BluetoothDevice device)
    {
        this.device = device;
        this.socket = null;
        this.reader = null;
        this.type = DEVICE_TYPE.valueOf(device.getName().trim());
        this.buffSize = this.type.getBuffSize();
        this.data = getDataFromType(this.type);
        this.connectionState = false;
    }

    public PuckPerfectData getDataFromType(DEVICE_TYPE type)
    {
        PuckPerfectData data = null;
        switch(this.type)
        {
            case CONES:
                data = new ConeData();
                break;
            case STICK:
                data = new StickData();
                break;
            case L_GLOVE:
            case R_GLOVE:
                data = new GloveData(this.type);
                break;
        }

        return data;
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

    public void setContext(Context context)
    {
        this.context = context;
    }

    public void createInputReader()
    {
        if(this.reader != null && this.reader.isRunning())
            this.reader.stop();

        this.reader = new InputReader();
    }

    public void startAcquire()
    {
        this.data.clear();
        this.acquire = true;
        createInputReader();
    }

    public void startAcquire(TextView debugTextView, ScrollView debugScrollView)
    {
        this.data.clear();
        this.acquire = true;

        if(this.reader != null && this.reader.isRunning())
            this.reader.stop();

        this.reader = new InputReader(debugTextView, debugScrollView);
    }

    /**
     * Stop the input thread
     */
    public void stopAcquire()
    {
        Log.i(TAG, "----- STOP ACQUIRE METHOD INVOKED -----");

        this.acquire = false;

        // Create a non-debug input reader for checking connection
        createInputReader();
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
        Log.i(TAG, "----- CONNECT METHOD INVOKED : " + device.getName().trim() + "-----");
        try {
            if(socket == null || !connectionState) {
                socket = device.createInsecureRfcommSocketToServiceRecord(deviceUUID);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                socket.connect();
                connectionState = true;

                createInputReader();
            }
        } catch(IOException e) {
            // Unable to connect to device
            e.printStackTrace();
            connectionState = false;
        }
    }

    public void exportData(Context context)
    {
        data.exportToFile(context);
    }


    @Override
    public String toString()
    {
        return "PuckPerfect Device: " + device.getName() + "\n" +
                "   Connection Status: " + connectionState + "\n";
    }

    // ----- Input Stream Reader Inner Class -----

    private class InputReader implements Runnable {

        private volatile boolean bStop = false;
        private Thread t;
        private TextView debugTextView = null;
        private ScrollView debugScrollView = null;
        private int timeout = 0;

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
                if(socket != null)
                {
                    inputStream = socket.getInputStream();
                    while (!bStop) {
                        byte[] buffer = new byte[type.getBuffSize()]; // fallback

                        if (inputStream != null && inputStream.available() > 0) {
                            timeout = 0;
                            inputStream.read(buffer);
                            int i = 0;
                            /*
                             * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
                             */
                            for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
                            }
                            final String strInput = new String(buffer, 0, i);

                            if(type == DEVICE_TYPE.L_GLOVE)
                                Log.i(TAG, "Input from " + device.getName().trim() + ": " + strInput);

                            // Check if string is null or empty --> if so, lost connection
                            if(strInput.isEmpty())
                            {
                                connectionState = false;
                                this.stop();
                            }

                            // Check if we should be acquiring data
                            if(acquire)
                            {
                                // data.storeData(strInput);
                                data.storeData(buffer);

                                // FOR DEBUG PURPOSES ONLY
                                if(debugTextView != null)
                                {
                                    if(!data.isEmpty())
                                    {
                                        final String dataDebug = data.printLastDataPoint();

                                        debugTextView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(dataDebug != null && !dataDebug.isEmpty())
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
                            }

                        }
                        else if(inputStream != null && inputStream.available() <= 0)
                        {
                            timeout++;
                            // Log.i(TAG, device.getName().trim() + ": Timeout " + timeout);
                        }
                        else if(timeout > 5)
                        {
                            timeout = 0;
                            Log.i(TAG, "Lost connection to " + device.getName().trim());
                            connectionState = false;
                            this.stop();
                        }
                        Thread.sleep(250); // TODO: Change back to 250
                    }
                }

            } catch (IOException e) {
                Log.i(TAG, "Lost connection to " + device.getName().trim());
                connectionState = false;
                e.printStackTrace();
            } catch (InterruptedException e) {
                Log.i(TAG, "Lost connection to " + device.getName().trim());
                e.printStackTrace();
                connectionState = false;
            }

        }

        public void stop() {
            Log.i(TAG, "----- INPUT READER: STOP METHOD INVOKED -----");
            bStop = true;
        }

    }
}
